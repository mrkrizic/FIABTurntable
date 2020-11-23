import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActor;
import akka.testkit.TestKit;
import components.conveyoractor.impl.ConveyorActorImpl;
import components.conveyoractor.stateMachine.ConveyorStates;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.requests.ConveyorRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ConveyorActorTest {

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
                TestKit tester = new TestKit(system);
                ActorRef subject = system.actorOf(Props.create(ConveyorActorImpl.class, () -> new ConveyorActorImpl(ActorRef.noSender(), tester.testActor())));
                tester.receiveN(2);
                //Skip subscription msg and stopped on creation
                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET), ActorRef.noSender());

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.RESETTING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_EMPTY, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.LOAD), ActorRef.noSender());
                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.LOADING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_LOADED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP), ActorRef.noSender());
                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET), ActorRef.noSender());
                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.RESETTING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_LOADED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.UNLOAD), ActorRef.noSender());
                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.UNLOADING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_EMPTY, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(3, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP), ActorRef.noSender());
                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPED, getCurrentStateFromTester(tester));
                    return null;
                });
            }

            ConveyorStates getCurrentStateFromTester(TestKit tester) {
                return ((ConveyorStatusUpdateNotification) tester.lastMessage().msg()).getConveyorState();
            }
        };
    }


}
