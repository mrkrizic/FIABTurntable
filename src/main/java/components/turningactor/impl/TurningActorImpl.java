package components.turningactor.impl;

import akka.actor.ActorRef;
import com.github.oxo42.stateless4j.StateMachine;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.conveyoractor.stateMachine.ConveyorTriggers;
import components.turningactor.TurningActorBase;
import components.turningactor.stateMachine.TurningStateMachine;
import components.turningactor.stateMachine.TurningStates;
import components.turningactor.stateMachine.TurningTriggers;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.TurnRequest;
import msg.requests.TurntableRequest;

import java.time.Duration;

public class TurningActorImpl extends TurningActorBase {

    private final TurningStateMachine stateMachine;
    private TurnRequest.Direction currentDirection;

    private TurnRequest.Direction from;
    private TurnRequest.Direction to;

    @Inject
    public TurningActorImpl(@Named("no-connector") ActorRef connectorTopDomain,
                            @Named("IntraMachineConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
        this.stateMachine = new TurningStateMachine();
        configureStateMachine();
        publishCurrentState();
    }

    private void configureStateMachine() {
        stateMachine.configure(TurningStates.STOPPING).onEntry(this::doStopping);
        stateMachine.configure(TurningStates.RESETTING).onEntry(this::doResetting);
        stateMachine.configure(TurningStates.STARTING).onEntry(this::doStarting);
        stateMachine.configure(TurningStates.EXECUTE).onEntry(this::doExecute);
        stateMachine.configure(TurningStates.COMPLETING).onEntry(this::doCompleting);
    }

    @RequestHandler
    public void handleTurnRequest(TurnRequest turnRequest) {
        fireStateMachineTrigger(TurningTriggers.DO_STARTING);
    }

    @RequestHandler
    public void handleTurntableRequest(TurntableRequest turntableRequest) {
        if (turntableRequest.getMessage().equals(BasicMachineRequests.SimpleMessageTypes.Reset)) {
            fireStateMachineTrigger(TurningTriggers.DO_RESETTING);
        } else if (turntableRequest.getMessage().equals(BasicMachineRequests.SimpleMessageTypes.Stop)) {
            fireStateMachineTrigger(TurningTriggers.DO_STOPPING);
        }
    }

    private void doStopping() {
        log.info("Stopping called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
                () -> fireStateMachineTrigger(TurningTriggers.DO_STOP),
                context().dispatcher());
    }

    private void doResetting() {
        log.info("Resetting called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
                () -> fireStateMachineTrigger(TurningTriggers.DO_IDLE),
                context().dispatcher());
    }

    private void doStarting() {
        log.info("Starting called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
                () -> fireStateMachineTrigger(TurningTriggers.DO_EXECUTE),
                context().dispatcher());
    }

    private void doExecute() {
        log.info("Execute called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
                () -> fireStateMachineTrigger(TurningTriggers.DO_COMPLETING),
                context().dispatcher());
    }

    private void doCompleting() {
        log.info("Completing called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
                () -> fireStateMachineTrigger(TurningTriggers.DO_COMPLETE),
                context().dispatcher());
    }

    private void publishCurrentState() {
        publishNotification(new TurntableStatusUpdateNotification(stateMachine.getState()));
    }

    private void fireStateMachineTrigger(TurningTriggers trigger) {
        if (stateMachine.canFire(trigger)) {
            stateMachine.fire(trigger);
            publishCurrentState();
        } else {
            log.info("Cannot fire " + trigger.name() + " from state " + stateMachine.getState());
        }
    }

}