package components;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.JavaPartialFunction;
import akka.testkit.TestKit;
import c2akka.c2architecture.C2StartMessage;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import c2akka.c2links.subscription.SubscriptionMessage;
import components.clienthandshakeeastactor.ClientHandshakeEastActorImpl;
import components.clienthandshakenorthactor.ClientHandshakeNorthActorImpl;
import components.clienthandshakesouthactor.ClientHandshakeSouthActorImpl;
import components.clienthandshakewestactor.ClientHandshakeWestActorImpl;
import components.conveyoractor.ConveyorActorImpl;
import components.transportmodulecoordinatoractor.TransportModuleCoordinatorActorImpl;
import components.turningactor.TurningActorImpl;
import connectors.handshakeconnector.HandshakeConnectorImpl;
import connectors.handshakeconnector.HandshakeConnectorImpl;
import connectors.intramachineconnector.IntraMachineConnectorImpl;
import fiab.core.capabilities.BasicMachineStates;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.handshake.actor.LocalEndpointStatus;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import hardware.ConveyorHardware;
import hardware.ConveyorHardwareConfig;
import hardware.TurningHardware;
import hardware.TurningHardwareConfig;
import modules.opcua.OpcUaWrapper;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;
import msg.requests.OpcUaTransportRequest;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransportCoordinatorTest {

    private ActorSystem system;
    private OpcUaWrapper wrapper;
    private TurningHardware turningHardware;
    private ConveyorHardware conveyorHardware;

    private final String northCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_NORTH_CLIENT;
    private final String southCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_SOUTH_CLIENT;
    private final String westCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_WEST_CLIENT;
    private final String eastCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_EAST_CLIENT;

    @Before
    public void setup() {
        wrapper = createOpcUaWrapper();
        turningHardware = new TurningHardwareConfig().getTurningHardware();
        conveyorHardware = new ConveyorHardwareConfig().getConveyorHardware();
        this.system = ActorSystem.create();
    }

    @After
    public void teardown() {
        system.terminate();
    }

    @Test
    public void testTransportNorthSouth() {
        testTransportCoordinatorAllStates(northCapability, southCapability);
    }

    @Test
    public void testTransportNorthEast() {
        testTransportCoordinatorAllStates(northCapability, eastCapability);
    }

    @Test
    public void testTransportNorthWest() {
        testTransportCoordinatorAllStates(northCapability, westCapability);
    }

    public void testTransportCoordinatorAllStates(String fromCap, String toCap) {
        new TestKit(system) {
            {
                List<ActorRef> subjects = new ArrayList<>();
                TestKit tester = new TestKit(system);
                ActorRef noConnector = system.actorOf(Props.create(C2EmptyConnector.class, C2EmptyConnector::new), "NoConn");
                ActorRef handshakeConnector = system.actorOf(Props.create(HandshakeConnectorImpl.class,
                        HandshakeConnectorImpl::new), "HandshakeConnector");
                subjects.add(handshakeConnector);
                ActorRef intraEventBus = system.actorOf(Props.create(IntraMachineConnectorImpl.class,
                        () -> new IntraMachineConnectorImpl(handshakeConnector, wrapper)), "IntraEventBus");
                subjects.add(intraEventBus);
                ActorRef conveyorFu = system.actorOf(Props.create(ConveyorActorImpl.class,
                        () -> new ConveyorActorImpl(noConnector, intraEventBus, conveyorHardware)), "ConveyorFu");
                subjects.add(conveyorFu);
                ActorRef turningFu = system.actorOf(Props.create(TurningActorImpl.class,
                        () -> new TurningActorImpl(noConnector, intraEventBus, turningHardware)), "TurningFu");
                subjects.add(turningFu);
                ActorRef hsFuNorth = system.actorOf(Props.create(ClientHandshakeNorthActorImpl.class,
                        () -> new ClientHandshakeNorthActorImpl(noConnector, handshakeConnector, wrapper)), "HsNorth");
                subjects.add(hsFuNorth);
                ActorRef hsFuEast = system.actorOf(Props.create(ClientHandshakeEastActorImpl.class,
                        () -> new ClientHandshakeEastActorImpl(noConnector, handshakeConnector, wrapper)), "HsEast");
                subjects.add(hsFuEast);
                ActorRef hsFuSouth = system.actorOf(Props.create(ClientHandshakeSouthActorImpl.class,
                        () -> new ClientHandshakeSouthActorImpl(noConnector, handshakeConnector, wrapper)), "HsSouth");
                subjects.add(hsFuSouth);
                ActorRef hsFuWest = system.actorOf(Props.create(ClientHandshakeWestActorImpl.class,
                        () -> new ClientHandshakeWestActorImpl(noConnector, handshakeConnector, wrapper)), "HsWest");
                subjects.add(hsFuWest);
                ActorRef subject = system.actorOf(Props.create(TransportModuleCoordinatorActorImpl.class,
                        () -> new TransportModuleCoordinatorActorImpl(intraEventBus, tester.testActor())),
                        "TransportCoordinator");
                subjects.add(subject);
                ActorRef hsListener = system.actorOf(Props.create(TestActor.class,
                        () -> new TestActor(handshakeConnector, tester.testActor(), tester.testActor())), "hsListener");
                subjects.add(hsListener);
                for (ActorRef ref : subjects) {
                    ref.tell(new C2StartMessage(), ActorRef.noSender());
                }
                FiniteDuration timeout = Duration.apply(30, TimeUnit.SECONDS);
                tester.receiveN(3);
                boolean isHandshakeReady = false;
                boolean isMachineReady = false;
                int hsCount = 0;
                while (!isHandshakeReady && !isMachineReady) {
                    Object msg = tester.receiveOne(timeout);
                    if (msg instanceof MachineStatusUpdateNotification) {
                        isMachineReady = true;
                    } else if (msg instanceof ClientHandshakeEndpointStatusNotification) {
                        hsCount++;
                        if (hsCount == 4) {
                            isHandshakeReady = true;
                        }
                    }
                }
                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.RESETTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.IDLE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaTransportRequest(fromCap, toCap,
                            "Order1"), ActorRef.noSender());
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.STARTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.EXECUTE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(30, TimeUnit.SECONDS), () -> {
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.COMPLETING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.COMPLETE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(30, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.RESETTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.IDLE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(30, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Stop), ActorRef.noSender());
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.STOPPING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.STOPPED.name(), getCurrentStateFromTester(tester));
                    return null;
                });

            }

            private String getCurrentStateFromTester(TestKit tester) {
                return ((MachineStatusUpdateNotification) tester.lastMessage().msg()).getState();
            }

        };
    }

    static class TestActor extends C2Component {

        private ActorRef tester;
        private int hsCounter;

        public TestActor(ActorRef connectorTopDomain, ActorRef connectorBottomDomain, ActorRef tester) {
            super(connectorTopDomain, connectorBottomDomain);
            this.tester = tester;
            this.hsCounter = 0;
        }

        @Override
        protected void addSupportedMessagesTopDomain() {
            supportedMessagesTop.add(ClientHandshakeEndpointStatusNotification.class);
        }

        @Override
        protected void addSupportedMessagesBottomDomain() {
            supportedMessagesBottom.add(ClientHandshakeEndpointStatusNotification.class);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(ClientHandshakeEndpointStatusNotification.class, msg -> {
                        hsCounter++;
                        if (hsCounter <= 4) {
                            tester.tell(msg, ActorRef.noSender());
                        }
                    })
                    .matchAny(msg -> {
                        log.info("Received msg " + msg);
                    })
                    .build();
        }
    }


    private OpcUaWrapper createOpcUaWrapper() {
        try {
            String machineName = "TestMachine";
            String machineNodePrefix = machineName + "/" + "Turntable_FU";
            OPCUABase opcuaBase = new OPCUABase(new NonEncryptionBaseOpcUaServer(0, machineName).getServer(),
                    "urn:factory-in-a-box", machineName);
            UaFolderNode rootNode = opcuaBase.prepareRootNode();
            UaFolderNode ttNode = opcuaBase.generateFolder(rootNode, machineName, "Turntable_FU");
            return new OpcUaWrapper(opcuaBase, rootNode, ttNode, machineName, machineNodePrefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
