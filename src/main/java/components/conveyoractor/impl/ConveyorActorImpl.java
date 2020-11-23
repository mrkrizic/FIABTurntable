package components.conveyoractor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.conveyoractor.ConveyorActorBase;
import components.conveyoractor.stateMachine.ConveyorStateMachine;
import components.conveyoractor.stateMachine.ConveyorStates;
import components.conveyoractor.stateMachine.ConveyorTriggers;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorTriggers;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.ConveyorRequest;

import java.time.Duration;

public class ConveyorActorImpl extends ConveyorActorBase {

    private final ConveyorStateMachine stateMachine;
    private boolean isLoaded;

    @Inject
    public ConveyorActorImpl(@Named("no-connector") ActorRef connectorTopDomain,
                             @Named("IntraMachineConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
        this.stateMachine = new ConveyorStateMachine();
        configureStateMachine();
        isLoaded = false;   //TODO use sensor value
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
        if (!isLoaded) {//TODO check Sensor value
            scheduler.scheduleOnce(Duration.ofSeconds(1),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_EMPTY),
                    context().dispatcher());
        } else {
            scheduler.scheduleOnce( Duration.ofSeconds(1),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_LOADED),
                    context().dispatcher());
        }

    }

    private void doLoading() {
        log.info("Loading called");
        if (!isLoaded) {
            isLoaded = true;
            scheduler.scheduleOnce(Duration.ofSeconds(1),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_LOADED),
                    context().dispatcher());
        }
    }

    private void doUnloading() {
        log.info("Unloading called");
        if (isLoaded) {
            isLoaded = false;
            scheduler.scheduleOnce(Duration.ofSeconds(1),
                    () -> fireStateMachineTrigger(ConveyorTriggers.DO_IDLE_EMPTY),
                    context().dispatcher());
        }
    }

    private void doStopping() {
        log.info("Stopping called");
        scheduler.scheduleOnce(Duration.ofSeconds(1),
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