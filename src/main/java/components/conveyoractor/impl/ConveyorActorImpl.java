package components.conveyoractor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.RequestHandler;
import components.conveyoractor.ConveyorActorBase;
import components.conveyoractor.stateMachine.ConveyorStateMachine;
import components.conveyoractor.stateMachine.ConveyorStates;
import components.conveyoractor.stateMachine.ConveyorTriggers;
import hardware.ConveyorHardware;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.requests.ConveyorRequest;

import java.time.Duration;

public class ConveyorActorImpl extends ConveyorActorBase {

    private final ConveyorStateMachine stateMachine;
    private final ConveyorHardware conveyorHardware;
    private boolean isLoaded;

    @Inject
    public ConveyorActorImpl(@Named("no-connector") ActorRef connectorTopDomain,
                             @Named("IntraMachineConnector") ActorRef connectorBottomDomain,
                             ConveyorHardware conveyorHardware) {
        super(connectorTopDomain, connectorBottomDomain);
        this.conveyorHardware = conveyorHardware;
        this.stateMachine = new ConveyorStateMachine();
        configureStateMachine();
        isLoaded = conveyorHardware.getUnloadingSensor().hasDetectedInput();
    }

    private void configureStateMachine() {
        stateMachine.configure(ConveyorStates.RESETTING)
                .onEntry(this::doResetting);
        stateMachine.configure(ConveyorStates.LOADING)
                .onEntry(this::doLoading);
        stateMachine.configure(ConveyorStates.UNLOADING)
                .onEntry(this::doUnloading);
        stateMachine.configure(ConveyorStates.STOPPING)
                .onEntry(this::doStopping);
        publishCurrentState();
    }

    @RequestHandler
    public void handleConveyorRequest(ConveyorRequest conveyorRequest) {
        if (conveyorRequest.getConveyorRequest().equals(ConveyorRequest.ConveyorRequests.RESET)) {
            fireStateMachineTrigger(ConveyorTriggers.DO_RESETTING);
        } else if (conveyorRequest.getConveyorRequest().equals(ConveyorRequest.ConveyorRequests.LOAD)) {
            fireStateMachineTrigger(ConveyorTriggers.DO_LOADING);
        } else if (conveyorRequest.getConveyorRequest().equals(ConveyorRequest.ConveyorRequests.UNLOAD)) {
            fireStateMachineTrigger(ConveyorTriggers.DO_UNLOADING);
        } else if (conveyorRequest.getConveyorRequest().equals(ConveyorRequest.ConveyorRequests.STOP)) {
            fireStateMachineTrigger(ConveyorTriggers.DO_STOPPING);
        }
    }

    private void doResetting() {
        log.info("Resetting called");
        isLoaded = conveyorHardware.getUnloadingSensor().hasDetectedInput();
        if (!isLoaded) {
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_EMPTY),
                    context().dispatcher());
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_LOADED),
                    context().dispatcher());
        }

    }

    private void doLoading() {
        log.info("Loading called");
        if (!conveyorHardware.getLoadingSensor().hasDetectedInput()) {
            conveyorHardware.getConveyorMotor().backward();
            waitForLoadingToFinish();
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_LOADED), context().dispatcher());
        }
    }

    private void waitForLoadingToFinish() {
        isLoaded = conveyorHardware.getLoadingSensor().hasDetectedInput();
        log.info("Waiting for loading to finish. Loading Sensor val: {}", isLoaded);
        if (isLoaded) {
            conveyorHardware.getConveyorMotor().stop();
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_LOADED),
                    context().dispatcher());
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100), this::waitForLoadingToFinish,
                    context().dispatcher());
        }
    }

    private void doUnloading() {
        log.info("Unloading called");
        if (conveyorHardware.getUnloadingSensor().hasDetectedInput()) {
            conveyorHardware.getConveyorMotor().forward();
            waitForUnloadingToFinish();
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_EMPTY), context().dispatcher());
        }
    }

    private void waitForUnloadingToFinish() {
        isLoaded = conveyorHardware.getUnloadingSensor().hasDetectedInput();
        if (!isLoaded) {
            conveyorHardware.getConveyorMotor().stop();
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_EMPTY), context().dispatcher());
        } else {
            scheduler.scheduleOnce(Duration.ofMillis(100),
                    this::waitForUnloadingToFinish, context().dispatcher());
        }
    }

    private void doStopping() {
        log.info("Stopping called");
        conveyorHardware.getConveyorMotor().stop();
        scheduler.scheduleOnce(Duration.ofMillis(100),
                () -> fireStateMachineTrigger(ConveyorTriggers.DO_STOP),
                context().dispatcher());
    }

    private void publishCurrentState() {
        publishNotification(new ConveyorStatusUpdateNotification(stateMachine.getState()));
    }

    private void fireStateMachineTrigger(ConveyorTriggers trigger) {
        if (stateMachine.canFire(trigger)) {
            stateMachine.fire(trigger);
            publishCurrentState();
        } else {
            log.info("Cannot fire " + trigger.name() + " from state " + stateMachine.getState());
        }
    }


}