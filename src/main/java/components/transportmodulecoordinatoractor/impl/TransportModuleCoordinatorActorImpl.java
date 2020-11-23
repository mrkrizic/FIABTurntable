package components.transportmodulecoordinatoractor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.conveyoractor.stateMachine.ConveyorStates;
import components.transportmodulecoordinatoractor.TransportModuleCoordinatorActorBase;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorStateMachine;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorStates;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorTriggers;
import components.turningactor.stateMachine.TurningStates;
import fiab.core.capabilities.BasicMachineStates;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import fiab.core.capabilities.handshake.HandshakeCapability;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.*;

import java.time.Duration;

public class TransportModuleCoordinatorActorImpl extends TransportModuleCoordinatorActorBase {

    private final TransportCoordinatorStateMachine stateMachine;
    private ConveyorStates conveyorState;
    private TurningStates turntableState;
    private TurnRequest.Direction source;
    private TurnRequest.Direction target;

    @Inject
    public TransportModuleCoordinatorActorImpl(@Named("IntraMachineConnector") ActorRef connectorTopDomain,
                                               @Named("OpcUaConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
        this.stateMachine = new TransportCoordinatorStateMachine();
        publishCurrentState();
        this.conveyorState = ConveyorStates.UNKNOWN;
        this.turntableState = TurningStates.UNKNOWN;
    }

    @RequestHandler
    public void handleOpcUaRequest(OpcUaRequest opcUaRequest) {
        log.info("Received OpcUaRequest: " + opcUaRequest.getMsg());
        if (opcUaRequest.getMsg().equals(HandshakeCapability.ServerMessageTypes.Stop)) {
            log.info("Received Stop");
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_STOPPING);
            publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP));
            publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Stop));
        } else if (opcUaRequest.getMsg().equals(HandshakeCapability.ServerMessageTypes.Reset)) {
            if (stateMachine.getState().equals(TransportCoordinatorStates.STOPPED)
                    || stateMachine.getState().equals(TransportCoordinatorStates.COMPLETE))
                fireStateMachineTrigger(TransportCoordinatorTriggers.DO_RESET);
            publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET));
            publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset));
            log.info("Received Reset");
        }
    }

    @RequestHandler
    public void handleInternalTransportModuleRequest(InternalTransportModuleRequest request) {
        fireStateMachineTrigger(TransportCoordinatorTriggers.DO_START);
        fireStateMachineTrigger(TransportCoordinatorTriggers.DO_EXECUTE);
        this.source = TurnRequest.Direction.valueOf(request.getCapIdFrom());
        this.target = TurnRequest.Direction.valueOf(request.getCapIdTo());
        publishRequest(new TurnRequest(source));    //Turn to source
        //fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETING);
        //fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETE);
        log.info("Received Internal Transport Module Request");
    }

    @NotificationHandler
    public void handleMachineStatusUpdateNotification(MachineStatusUpdateNotification machineStatusUpdateNotification) {
        //TODO remove notifications on bottom port, remove requests from top port
        //TODO add notifications from top port
    }

    @NotificationHandler
    public void handleConveyorStatusUpdateNotification(ConveyorStatusUpdateNotification conveyorStatusUpdateNotification) {
        conveyorState = conveyorStatusUpdateNotification.getConveyorState();
        log.info("Received Conveyor Status update: " + conveyorStatusUpdateNotification.getConveyorState());
        checkForTransition();
        if (stateMachine.getState().equals(TransportCoordinatorStates.EXECUTE)) {
            if (conveyorState.equals(ConveyorStates.IDLE_LOADED)) {
                publishRequest(new TurnRequest(target));    //If conveyor is loaded we can turn to destination
            } else if (conveyorState.equals(ConveyorStates.IDLE_EMPTY)) {
                fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETING);
                scheduler.scheduleOnce(Duration.ofSeconds(1),
                        () -> fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETE),
                        context().dispatcher());  //We don't need to do anything in completing
            }
        }
    }

    @NotificationHandler
    public void handleTurntableStatusUpdateNotification(TurntableStatusUpdateNotification turntableStatusUpdateNotification) {
        turntableState = turntableStatusUpdateNotification.getState();
        log.info("Received Turning Status update: " + turntableStatusUpdateNotification.getState());
        checkForTransition();
        if (stateMachine.getState().equals(TransportCoordinatorStates.EXECUTE)
                && turntableStatusUpdateNotification.getState().equals(TurningStates.COMPLETE)) {
            if (conveyorState.equals(ConveyorStates.IDLE_EMPTY)) {    //source
                publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.LOAD)); //when reached position, load conveyor
                publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset));    //reset tt in meantime
            } else if (conveyorState.equals(ConveyorStates.IDLE_LOADED)) {  //target
                publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.UNLOAD));   //unload conveyor as it reached destination
                //no need for resetting tt as it will be after coordinator complete
            }
        }
    }

    private void checkForTransition() {
        transitionToIdleOrStoppedIfReady();
    }

    private void transitionToIdleOrStoppedIfReady() {
        if ((conveyorState.equals(ConveyorStates.IDLE_EMPTY) || conveyorState.equals(ConveyorStates.IDLE_LOADED))
                && turntableState.equals(TurningStates.IDLE)
                && stateMachine.getState().equals(TransportCoordinatorStates.RESETTING)) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_IDLE);
        } else if (conveyorState.equals(ConveyorStates.STOPPED) && turntableState.equals(TurningStates.STOPPED)
                && stateMachine.getState().equals(TransportCoordinatorStates.STOPPING)) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_STOP);
        }
    }

    private void publishCurrentState() {
        publishNotification(new MachineStatusUpdateNotification(stateMachine.getState().name()));
    }

    private void fireStateMachineTrigger(TransportCoordinatorTriggers trigger) {
        if (stateMachine.canFire(trigger)) {
            stateMachine.fire(trigger);
            publishCurrentState();
        } else {
            log.info("Cannot fire " + trigger.name() + " from state " + stateMachine.getState());
        }
    }

}