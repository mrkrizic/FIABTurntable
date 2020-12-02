import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import components.turningactor.impl.TurningActorImpl;
import components.turningactor.stateMachine.TurningStates;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import hardware.TurningHardware;
import hardware.TurningHardwareConfig;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.TurnRequest;
import msg.requests.TurntableRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TurningActorTest {

    private ActorSystem system;
    private TurningHardware turningHardware;

    @Before
    public void setup() {
        turningHardware = new TurningHardwareConfig().getTurningHardware();
        this.system = ActorSystem.create();
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void testTurningActorAllTransitions() {
        new TestKit(system) {
            {
                TestKit tester = new TestKit(system);
                FiniteDuration timeout = FiniteDuration.apply(10, TimeUnit.SECONDS);
                ActorRef subject = system.actorOf(Props.create(TurningActorImpl.class,
                        () -> new TurningActorImpl(null, tester.testActor(), turningHardware)), "Turning");
                tester.receiveN(2);     //Skip subscription and initial state

                within(timeout, () -> {
                    subject.tell(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.RESETTING, getMessageFromTester(tester));
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.IDLE, getMessageFromTester(tester));
                    return null;
                });

                within(timeout, () -> {
                    subject.tell(new TurnRequest(TurnRequest.Direction.WEST), ActorRef.noSender());
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.STARTING, getMessageFromTester(tester));
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.EXECUTE, getMessageFromTester(tester));
                    return null;
                });

                within(timeout, () -> {
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.COMPLETING, getMessageFromTester(tester));
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.COMPLETE, getMessageFromTester(tester));
                    return null;
                });

                within(timeout, () -> {
                    subject.tell(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset), ActorRef.noSender());
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.RESETTING, getMessageFromTester(tester));
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.IDLE, getMessageFromTester(tester));
                    return null;
                });

                within(timeout, () -> {
                    subject.tell(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Stop), ActorRef.noSender());
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.STOPPING, getMessageFromTester(tester));
                    tester.expectMsgClass(timeout, TurntableStatusUpdateNotification.class);
                    Assert.assertEquals(TurningStates.STOPPED, getMessageFromTester(tester));
                    return null;
                });
                tester.expectNoMessage();
            }

            private TurningStates getMessageFromTester(TestKit tester) {
                return ((TurntableStatusUpdateNotification) tester.lastMessage().msg()).getState();
            }
        };
    }
}
