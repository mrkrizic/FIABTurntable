package components.turningactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.RequestHandler;
import components.turningactor.TurningActorBase;
import components.turningactor.stateMachine.TurningStateMachine;
import components.turningactor.stateMachine.TurningStates;
import components.turningactor.stateMachine.TurningTriggers;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import hardware.TurningHardware;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.TurnRequest;
import msg.requests.TurntableRequest;

import java.time.Duration;

public class TurningActorImpl extends TurningActorBase {

    private final TurningStateMachine stateMachine;
    private int targetAngle;

    private final TurningHardware turningHardware;

    @Inject
    public TurningActorImpl(@Named("no-connector") ActorRef connectorTopDomain,
                            @Named("IntraMachineConnector") ActorRef connectorBottomDomain
            , TurningHardware turningHardware) {
        super(connectorTopDomain, connectorBottomDomain);
        this.turningHardware = turningHardware;
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
        if (fireStateMachineTrigger(TurningTriggers.DO_STARTING)) {
            targetAngle = getAngleFromDirection(turnRequest.getTarget());
        }
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
        turningHardware.getTurningMotor().stop();
        scheduler.scheduleOnce(Duration.ofMillis(100),
                () -> fireStateMachineTrigger(TurningTriggers.DO_STOP),
                context().dispatcher());
    }

    private void doResetting() {
        log.info("Resetting called");
        turningHardware.getTurningMotor().backward();
        waitForHomingPosReached();
    }

    private void waitForHomingPosReached() {
        if (turningHardware.getSensorHoming().hasDetectedInput()) {
            turningHardware.getTurningMotor().stop();
            turningHardware.getTurningMotor().resetTachoCount();
            turningHardware.getTurningMotor().setSpeed(300);    //Resets after tacho reset
            log.info("Reached Homing Position");
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(TurningTriggers.DO_IDLE),
                    context().dispatcher());
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100), this::waitForHomingPosReached,
                    context().dispatcher());
        }
    }

    private void doStarting() {
        log.info("Starting called");
        scheduler.scheduleOnce(Duration.ofMillis(100),
                () -> fireStateMachineTrigger(TurningTriggers.DO_EXECUTE),
                context().dispatcher());
    }

    private void doExecute() {
        log.info("Execute called");
        turningHardware.getTurningMotor().forward();
        waitForTargetPositionReached();
    }

    private void waitForTargetPositionReached() {
        if (turningHardware.getTurningMotor().getRotationAngle() >= targetAngle - 5) {  //remove rounding error from casting float -> int
            log.info("Turning position reached");
            turningHardware.getTurningMotor().stop();
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(TurningTriggers.DO_COMPLETING),
                    context().dispatcher());
        } else {
            log.info("Current pos {}, Target pos {}", turningHardware.getTurningMotor().getRotationAngle(), targetAngle);
            scheduler.scheduleOnce(Duration.ofMillis(100), this::waitForTargetPositionReached,
                    context().dispatcher());
        }
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

    private boolean fireStateMachineTrigger(TurningTriggers trigger) {
        if (stateMachine.canFire(trigger)) {
            stateMachine.fire(trigger);
            publishCurrentState();
            return true;
        } else {
            log.info("Cannot fire " + trigger.name() + " from state " + stateMachine.getState());
            return false;
        }
    }

    private int getAngleFromDirection(TurnRequest.Direction direction) {
        int rightAngle = 225;       //When combining gears of different sized ra != 90
        int SOUTH = rightAngle * 2 - 15;
        int WEST = rightAngle * 3 - 20;
        int HOME = 0;
        switch (direction) {
            case NORTH:
                return HOME;
            case EAST:
                return rightAngle;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            default:
                return 0;
        }
    }

}