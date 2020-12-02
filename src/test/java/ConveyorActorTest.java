import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActor;
import akka.testkit.TestKit;
import components.conveyoractor.impl.ConveyorActorImpl;
import components.conveyoractor.stateMachine.ConveyorStates;
import hardware.ConveyorHardware;
import hardware.ConveyorHardwareConfig;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.requests.ConveyorRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ConveyorActorTest {

    private ActorSystem system;
    private ConveyorHardware conveyorHardware;

    @Before
    public void setup() {
        this.system = ActorSystem.create();
        this.conveyorHardware = new ConveyorHardwareConfig().getConveyorHardware();
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
                FiniteDuration timeout = FiniteDuration.apply(10, TimeUnit.SECONDS);
                ActorRef subject = system.actorOf(Props.create(ConveyorActorImpl.class,
                        () -> new ConveyorActorImpl(ActorRef.noSender(), tester.testActor(), conveyorHardware)));
                tester.receiveN(2);
                //Skip subscription msg and stopped on creation
                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET), ActorRef.noSender());

                    tester.expectMsgClass(timeout, ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.RESETTING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_EMPTY, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.LOAD), ActorRef.noSender());
                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.LOADING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_LOADED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP), ActorRef.noSender());
                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET), ActorRef.noSender());
                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.RESETTING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(timeout, ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_LOADED, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.UNLOAD), ActorRef.noSender());
                    tester.expectMsgClass(timeout, ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.UNLOADING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.IDLE_EMPTY, getCurrentStateFromTester(tester));
                    return null;
                });

                within(FiniteDuration.apply(15, TimeUnit.SECONDS), () -> {
                    subject.tell(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP), ActorRef.noSender());
                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
                    Assert.assertEquals(ConveyorStates.STOPPING, getCurrentStateFromTester(tester));

                    tester.expectMsgClass(timeout,ConveyorStatusUpdateNotification.class);
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
