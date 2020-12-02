import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActor;
import akka.testkit.TestKit;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2connector.C2Connector;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import c2akka.c2messages.C2Message;
import c2akka.c2messages.C2Notification;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import components.clienthandshakeeastactor.impl.ClientHandshakeEastActorImpl;
import components.clienthandshakenorthactor.impl.ClientHandshakeNorthActorImpl;
import components.clienthandshakesouthactor.impl.ClientHandshakeSouthActorImpl;
import components.clienthandshakewestactor.impl.ClientHandshakeWestActorImpl;
import components.conveyoractor.impl.ConveyorActorImpl;
import components.transportmodulecoordinatoractor.impl.TransportModuleCoordinatorActorImpl;
import components.turningactor.impl.TurningActorImpl;
import connectors.handshakeconnector.HandshakeConnector;
import connectors.intramachineconnector.IntraMachineConnector;
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
import junit.extensions.RepeatedTest;
import junit.framework.JUnit4TestAdapter;
import modules.opcua.OpcUaWrapper;
import msg.notifications.ClientHandshakeEndpointNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TransportCoordinatorTest {

    private ActorSystem system;
    private OpcUaWrapper wrapper;
    private TurningHardware turningHardware;
    private ConveyorHardware conveyorHardware;

    private final String northCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_NORTH_CLIENT;
    private final String southCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_SOUTH_CLIENT;
    private final String westCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_WEST_CLIENT;
    private final String eastCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_EAST_CLIENT;

    //private Set<String> directions;

    @Before
    public void setup() {
        wrapper = createOpcUaWrapper();
        turningHardware = new TurningHardwareConfig().getTurningHardware();
        conveyorHardware = new ConveyorHardwareConfig().getConveyorHardware();
        this.system = ActorSystem.create();
        //this.directions = Set.of(northCapability, southCapability, westCapability, eastCapability);
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
                TestKit tester = new TestKit(system);
                ActorRef noConnector = system.actorOf(Props.create(C2EmptyConnector.class, C2EmptyConnector::new));
                ActorRef handshakeConnector = system.actorOf(Props.create(HandshakeConnector.class,
                        HandshakeConnector::new), "handshakeConnector");
                ActorRef intraEventBus = system.actorOf(Props.create(IntraMachineConnector.class,
                        () -> new IntraMachineConnector(handshakeConnector, wrapper)), "intraEventBus");
                ActorRef conveyorFu = system.actorOf(Props.create(ConveyorActorImpl.class,
                        () -> new ConveyorActorImpl(noConnector, intraEventBus, conveyorHardware)), "ConveyorFu");
                ActorRef turningFu = system.actorOf(Props.create(TurningActorImpl.class,
                        () -> new TurningActorImpl(noConnector, intraEventBus, turningHardware)), "TurningFu");
                ActorRef hsFuNorth = system.actorOf(Props.create(ClientHandshakeNorthActorImpl.class,
                        () -> new ClientHandshakeNorthActorImpl(noConnector, handshakeConnector, wrapper)), "HsNorth");
                ActorRef hsFuEast = system.actorOf(Props.create(ClientHandshakeEastActorImpl.class,
                        () -> new ClientHandshakeEastActorImpl(noConnector, handshakeConnector, wrapper)), "HsEast");
                ActorRef hsFuSouth = system.actorOf(Props.create(ClientHandshakeSouthActorImpl.class,
                        () -> new ClientHandshakeSouthActorImpl(noConnector, handshakeConnector, wrapper)), "HsSouth");
                ActorRef hsFuWest = system.actorOf(Props.create(ClientHandshakeWestActorImpl.class,
                        () -> new ClientHandshakeWestActorImpl(noConnector, handshakeConnector, wrapper)), "HsWest");
                ActorRef subject = system.actorOf(Props.create(TransportModuleCoordinatorActorImpl.class,
                        () -> new TransportModuleCoordinatorActorImpl(intraEventBus, tester.testActor())),
                        "TransportCoordinator");
                ActorRef hsListener = system.actorOf(Props.create(TestActor.class,
                        () -> new TestActor(handshakeConnector, tester.testActor())));
                FiniteDuration timeout = Duration.apply(30, TimeUnit.SECONDS);
                tester.receiveN(3); //skip sub msg and forward to stopped
                Assert.assertEquals(BasicMachineStates.STOPPED.name(), getCurrentStateFromTester(tester));
                tester.expectMsgClass(timeout, ClientHandshakeEndpointNotification.class);
                tester.expectMsgClass(timeout, ClientHandshakeEndpointNotification.class);
                tester.expectMsgClass(timeout, ClientHandshakeEndpointNotification.class);
                tester.expectMsgClass(timeout, ClientHandshakeEndpointNotification.class);
                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.RESETTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(timeout, MachineStatusUpdateNotification.class);
                    Assert.assertEquals(BasicMachineStates.IDLE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new InternalTransportModuleRequest(fromCap, toCap,
                            "Order1", "Req1"), ActorRef.noSender());
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
                System.out.println(tester.lastMessage().msg().getClass());
                return ((MachineStatusUpdateNotification) tester.lastMessage().msg()).getState();
            }


        };
    }

    static class TestActor extends C2Component {

        /*boolean waitingForNorthHs;
        boolean waitingForSouthHs;
        private String northCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_NORTH_CLIENT;
        private String southCapability = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_SOUTH_CLIENT;*/
        public TestActor(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
            super(connectorTopDomain, connectorBottomDomain);
        }

        @Override
        public Receive createReceive() {
            /*if (waitingForNorthHs && notification.getStatus().getCapabilityId().equals(northCapability)) {
                            waitingForNorthHs = false;
                            publishNotification(notification);

                        } else if (waitingForSouthHs && notification.getStatus().getCapabilityId().equals(southCapability)) {
                            waitingForSouthHs = false;
                            publishNotification(notification);
                        }*/
            return receiveBuilder()
                    .match(ClientHandshakeEndpointNotification.class, this::publishNotification)
                    .matchAny(msg -> {/*ignore*/})
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
