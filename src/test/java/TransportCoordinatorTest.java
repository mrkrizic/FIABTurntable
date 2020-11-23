import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import components.conveyoractor.impl.ConveyorActorImpl;
import components.transportmodulecoordinatoractor.impl.TransportModuleCoordinatorActorImpl;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorStates;
import components.turningactor.impl.TurningActorImpl;
import components.turningactor.stateMachine.TurningStates;
import connectors.intramachineconnector.IntraMachineConnector;
import fiab.core.capabilities.handshake.HandshakeCapability;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TransportCoordinatorTest {

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
    public void testTransportCoordinatorAllStates() {
        new TestKit(system) {
            {
                TestKit tester = new TestKit(system);
                ActorRef noConnector = system.actorOf(Props.create(C2EmptyConnector.class, C2EmptyConnector::new));
                ActorRef intraEventBus = system.actorOf(Props.create(IntraMachineConnector.class,
                        () -> new IntraMachineConnector(noConnector)), "intraEventBus");
                ActorRef conveyorFu = system.actorOf(Props.create(ConveyorActorImpl.class,
                        () -> new ConveyorActorImpl(noConnector, intraEventBus)), "ConveyorFu");
                ActorRef turningFu = system.actorOf(Props.create(TurningActorImpl.class,
                        () -> new TurningActorImpl(noConnector, intraEventBus)), "TurningFu");
                ActorRef subject = system.actorOf(Props.create(TransportModuleCoordinatorActorImpl.class,
                        () -> new TransportModuleCoordinatorActorImpl(intraEventBus, tester.testActor())),
                        "TransportCoordinator");
                tester.receiveN(2); //Skip first two messages and check if second is STOPPED
                Assert.assertEquals(TransportCoordinatorStates.STOPPED.name(), getCurrentStateFromTester(tester));

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.RESETTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.IDLE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    //TODO change cap if to use endpoints, then use hsFu to get direction
                    subject.tell(new InternalTransportModuleRequest("NORTH", "SOUTH", "order1", "req1"), ActorRef.noSender());
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.STARTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.EXECUTE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    tester.expectMsgClass(Duration.apply(10, TimeUnit.SECONDS), MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.COMPLETING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(Duration.apply(10, TimeUnit.SECONDS), MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.COMPLETE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.RESETTING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.IDLE.name(), getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Stop), ActorRef.noSender());
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.STOPPING.name(), getCurrentStateFromTester(tester));
                    tester.expectMsgClass(MachineStatusUpdateNotification.class);
                    Assert.assertEquals(TransportCoordinatorStates.STOPPED.name(), getCurrentStateFromTester(tester));
                    return null;
                });

            }

            private String getCurrentStateFromTester(TestKit tester) {
                return ((MachineStatusUpdateNotification) tester.lastMessage().msg()).getState();
            }
        };
    }
}
