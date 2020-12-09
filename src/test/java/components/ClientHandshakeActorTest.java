package components;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import c2akka.c2architecture.C2StartMessage;
import components.clienthandshakenorthactor.ClientHandshakeNorthActorImpl;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import modules.opcua.OpcUaWrapper;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.notifications.ClientHandshakeNotification;
import msg.requests.ClientHandshakeRequest;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class ClientHandshakeActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        this.system = ActorSystem.create();
    }

    @After
    public void teardown() {
        system.terminate();
    }

    @Test
    public void testResetRequestTransitionToIdle() {
        new TestKit(system) {
            {
                try {
                    FiniteDuration timeout = Duration.apply(5, TimeUnit.SECONDS);
                    String capabilityId = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_NORTH_CLIENT;
                    String machineName = "TestMachine";
                    String machineNodePrefix = machineName + "/" + "Mock_FU";
                    OPCUABase opcuaBase;
                    opcuaBase = new OPCUABase(new NonEncryptionBaseOpcUaServer(0, machineName).getServer(),
                            "urn:factory-in-a-box", machineName);
                    UaFolderNode rootNode = opcuaBase.prepareRootNode();
                    UaFolderNode machineNode = opcuaBase.generateFolder(rootNode, machineNodePrefix, "TEST_FU");
                    OpcUaWrapper wrapper = new OpcUaWrapper(opcuaBase, rootNode, machineNode, machineName, machineNodePrefix);
                    new Thread(opcuaBase).start();
                    TestKit tester = new TestKit(system);
                    ActorRef subject = system.actorOf(Props.create(ClientHandshakeNorthActorImpl.class, () -> new ClientHandshakeNorthActorImpl(ActorRef.noSender(), tester.testActor(), wrapper)));
                    subject.tell(new C2StartMessage(), ActorRef.noSender());
                    tester.receiveN(2);
                    Assert.assertEquals(HandshakeCapability.ClientSideStates.STOPPED, ((ClientHandshakeEndpointStatusNotification)tester.lastMessage().msg()).getStatus().getState());
                    subject.tell(new ClientHandshakeRequest(capabilityId, HandshakeCapability.ClientMessageTypes.Reset), ActorRef.noSender());
                    tester.receiveN(1);
                    Assert.assertEquals(HandshakeCapability.ClientSideStates.RESETTING, ((ClientHandshakeNotification)tester.lastMessage().msg()).getState());
                    tester.receiveN(1);
                    Assert.assertEquals(HandshakeCapability.ClientSideStates.IDLE, ((ClientHandshakeNotification)tester.lastMessage().msg()).getState());
                    System.out.println(tester.lastMessage().msg());
                    subject.tell(new ClientHandshakeRequest(capabilityId, HandshakeCapability.ClientMessageTypes.Start), ActorRef.noSender());
                    tester.receiveN(1);
                    Assert.assertEquals(HandshakeCapability.ClientSideStates.STARTING, ((ClientHandshakeNotification)tester.lastMessage().msg()).getState());
                    System.out.println(tester.lastMessage().msg());
                    tester.receiveN(1);
                    Assert.assertEquals(HandshakeCapability.ClientSideStates.INITIATING, ((ClientHandshakeNotification)tester.lastMessage().msg()).getState());
                    System.out.println(tester.lastMessage().msg());
                    subject.tell(new ClientHandshakeRequest(capabilityId, HandshakeCapability.ClientMessageTypes.Complete), ActorRef.noSender());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
