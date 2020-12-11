package components.transportmodulecoordinatoractor;

import akka.actor.ActorRef;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.conveyoractor.stateMachine.ConveyorStates;
import components.transportmodulecoordinatoractor.base.TransportModuleCoordinatorActorBase;
import components.transportmodulecoordinatoractor.handshake.EndpointToDirectionMapper;
import components.transportmodulecoordinatoractor.handshake.HandshakeEndpointInfo;
import components.transportmodulecoordinatoractor.stateMachine.InternalProcess;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorStateMachine;
import components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorTriggers;
import components.turningactor.stateMachine.TurningStates;
import fiab.core.capabilities.BasicMachineStates;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.handshake.actor.LocalEndpointStatus;
import msg.notifications.*;
import msg.requests.*;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class TransportModuleCoordinatorActorImpl extends TransportModuleCoordinatorActorBase {

    private final HandshakeEndpointInfo handshakeEndpointInfo;
    private final Set<BasicMachineStates> hsEndpointNonUpdatableStates = Sets.immutableEnumSet(BasicMachineStates.STARTING, BasicMachineStates.EXECUTE, BasicMachineStates.COMPLETING);
    private final TransportCoordinatorStateMachine stateMachine;
    private ConveyorStates conveyorState;
    private TurningStates turntableState;
    private InternalProcess currentProcessStep;
    private OpcUaTransportRequest currentRequest;

    @Inject
    public TransportModuleCoordinatorActorImpl(@Named("IntraMachineConnector") ActorRef connectorTopDomain,
                                               @Named("OpcUaConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
        this.stateMachine = new TransportCoordinatorStateMachine();
        this.handshakeEndpointInfo = new HandshakeEndpointInfo(self());
        this.currentProcessStep = InternalProcess.NOPROC;
        this.conveyorState = ConveyorStates.UNKNOWN;
        this.turntableState = TurningStates.UNKNOWN;
        scheduler.scheduleOnce(Duration.ofSeconds(1), this::publishCurrentState,
                context().dispatcher());
    }

    @RequestHandler
    public void handleOpcUaTransportRequest(OpcUaTransportRequest opcUaTransportRequest) {
        fireStateMachineTrigger(TransportCoordinatorTriggers.DO_START);
        turnToSource(opcUaTransportRequest);
        log.info("Received Internal Transport Module Request");
        publishNotification(new MachineStatusUpdateNotification(stateMachine.getState().name()));
    }

    @RequestHandler
    public void handleOpcUaRequest(OpcUaRequest opcUaRequest) {
        log.info("Received OpcUaRequest: " + opcUaRequest.getMsg());
        if (opcUaRequest.getMsg().equals(HandshakeCapability.ServerMessageTypes.Stop)) {
            stop();
        } else if (opcUaRequest.getMsg().equals(HandshakeCapability.ServerMessageTypes.Reset)) {
            reset();
            log.info("Received Reset");
        }
    }

    @Override
    public void handleClientHandshakeNotification(ClientHandshakeNotification clientHandshakeNotification) {
        log.info("Recieved Upadate from HS: " + clientHandshakeNotification.getState());
        handshakeEndpointInfo.getHandshakeEP(clientHandshakeNotification.getCapabilityId()).ifPresent(leps -> {
            ((LocalEndpointStatus.LocalClientEndpointStatus) leps).setState(clientHandshakeNotification.getState());
            switch (clientHandshakeNotification.getState()) {
                case COMPLETED:
                    if (currentProcessStep.equals(InternalProcess.HANDSHAKE_SOURCE)) {
                        loadConveyorFromSource();
                    } else if (currentProcessStep.equals(InternalProcess.HANDSHAKE_DEST)) {
                        unloadConveyorAtDestination();
                    }
                    break;
                case IDLE:
                    publishRequest(new ClientHandshakeRequest(clientHandshakeNotification.getCapabilityId(), HandshakeCapability.ClientMessageTypes.Start));
                    break;
                default:
                    break;
            }
        });
    }

    @NotificationHandler
    public void handleClientHandshakeEndpointStatusNotification(ClientHandshakeEndpointStatusNotification clientHandshakeEndpointStatusNotification) {
        log.info("Received Endpoint status from " + clientHandshakeEndpointStatusNotification.getStatus().getCapabilityId() +
                " with value:" + clientHandshakeEndpointStatusNotification.getStatus().getState());
        if (!hsEndpointNonUpdatableStates.contains(stateMachine.getState())) {
            this.handshakeEndpointInfo.addOrReplace(clientHandshakeEndpointStatusNotification.getStatus());
        } else {
            log.warning("Trying to update Handshake Endpoints in nonupdateable state: " + stateMachine.getState());
        }
    }

    @NotificationHandler
    public void handleConveyorStatusUpdateNotification(ConveyorStatusUpdateNotification conveyorStatusUpdateNotification) {
        conveyorState = conveyorStatusUpdateNotification.getConveyorState();
        log.info("Received Conveyor Status update: " + conveyorStatusUpdateNotification.getConveyorState());
        checkForTransition();
        if (stateMachine.getState().equals(BasicMachineStates.EXECUTE)) {
            if (conveyorState.equals(ConveyorStates.IDLE_LOADED)) {
                prepareForDestination();
            } else if (conveyorState.equals(ConveyorStates.IDLE_EMPTY)) {
                fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETING);
                scheduler.scheduleOnce(Duration.ofSeconds(1),
                        () -> fireStateMachineTrigger(TransportCoordinatorTriggers.DO_COMPLETE),
                        context().dispatcher());  //We don't need to do anything in completing
            }
        }
    }

    @NotificationHandler
    public void handleServerHandshakeNotification(ServerHandshakeNotification serverHandshakeNotification) {
        //TODO implement method stub
    }

    @NotificationHandler
    public void handleServerHandshakeEndpointStatusNotification(ServerHandshakeEndpointStatusNotification serverHandshakeEndpointStatusNotification) {
        //TODO implement method stub
    }

    @NotificationHandler
    public void handleTurntableStatusUpdateNotification(TurntableStatusUpdateNotification turntableStatusUpdateNotification) {
        turntableState = turntableStatusUpdateNotification.getState();
        log.info("Received Turning Status update: " + turntableStatusUpdateNotification.getState());
        checkForTransition();
        if (stateMachine.getState().equals(BasicMachineStates.EXECUTE)
                && turntableStatusUpdateNotification.getState().equals(TurningStates.COMPLETE)) {
            if (currentProcessStep.equals(InternalProcess.TURNING_SOURCE)) {
                startSourceHandshake();
            }else if (currentProcessStep.equals(InternalProcess.TURNING_DEST)){
                startDestinationHandshake();
            }
        }else if(stateMachine.getState().equals(BasicMachineStates.EXECUTE)
                && turntableStatusUpdateNotification.getState().equals(TurningStates.IDLE)
                && currentProcessStep.equals(InternalProcess.CONVEYING_SOURCE)){
            turnToDestination();
        }
    }

    private void checkForTransition() {
        transitionToIdleOrStoppedIfReady();
    }

    private void transitionToIdleOrStoppedIfReady() {
        if ((conveyorState.equals(ConveyorStates.IDLE_EMPTY) || conveyorState.equals(ConveyorStates.IDLE_LOADED))
                && turntableState.equals(TurningStates.IDLE)
                && stateMachine.getState().equals(BasicMachineStates.RESETTING)) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_IDLE);
        } else if (conveyorState.equals(ConveyorStates.STOPPED) && turntableState.equals(TurningStates.STOPPED)
                && stateMachine.getState().equals(BasicMachineStates.STOPPING)) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_STOP);
        }
    }

    private void startSourceHandshake() {
        Optional<LocalEndpointStatus> fromEP = handshakeEndpointInfo.getHandshakeEP(currentRequest.getCapabilityIdFrom());
        if (fromEP.isPresent()) {
            fromEP.ifPresent(leps -> {
                currentProcessStep = InternalProcess.HANDSHAKE_SOURCE;
                // now check if localEP is client or server, then reset
                if (leps.isProvidedCapability()) {
                    //TODO ServerSide
                    //publishRequest(new ServerHandshakeRequest(leps.getCapabilityId(), HandshakeCapability.ServerMessageTypes.Reset));
                } else {
                    publishRequest(new ClientHandshakeRequest(leps.getCapabilityId(), HandshakeCapability.ClientMessageTypes.Reset));
                }
            });
        }
    }

    private void turnToSource(OpcUaTransportRequest request) {
        this.currentRequest = request;
        Optional<LocalEndpointStatus> fromEP = handshakeEndpointInfo.getHandshakeEP(request.getCapabilityIdFrom());
        Optional<LocalEndpointStatus> toEP = handshakeEndpointInfo.getHandshakeEP(request.getCapabilityIdTo());
        if (fromEP.isPresent() && toEP.isPresent()) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_EXECUTE);
            publishRequest(new TurnRequest(EndpointToDirectionMapper.mapEndpointToDirection(fromEP.get())));    //Turn to source
            currentProcessStep = InternalProcess.TURNING_SOURCE;
        } else {
            log.warning("A HandshakeEndpoint could not be identified! From: " + fromEP.isPresent() + ", To: " + toEP.isPresent());
            log.info("Available HandshakeEndpoint infos: " + handshakeEndpointInfo.getAvailableEndpoints());
            if (!fromEP.isPresent())
                log.warning("Unknown HandshakeEndpoint identified by CapabilityId " + request.getCapabilityIdFrom());
            if (!toEP.isPresent())
                log.warning("Unknown HandshakeEndpoint identified by CapabilityId " + request.getCapabilityIdTo());
            stop();
        }
    }

    private void startDestinationHandshake(){
        log.info("Continuing with Destination Handshake via: " + currentRequest.getCapabilityIdTo());
        Optional<LocalEndpointStatus> toEP = handshakeEndpointInfo.getHandshakeEP(currentRequest.getCapabilityIdTo());
        toEP.ifPresent(leps -> {
            currentProcessStep = InternalProcess.HANDSHAKE_DEST;
            if (leps.isProvidedCapability()) {
                //TODO Serverside
                //publishRequest(new ServerHandshakeRequest(leps.getCapabilityId(), HandshakeCapability.ServerMessageTypes.Reset));
                //publishRequest(new ServerHandshakeOverrideRequest(leps.getCapabilityId(), HandshakeCapability.StateOverrideRequests.SetLoaded));
            } else {
                publishRequest(new ClientHandshakeRequest(leps.getCapabilityId(), HandshakeCapability.ClientMessageTypes.Reset));
            }
        });
    }

    private void prepareForDestination() {
        publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset));
    }

    private void turnToDestination() {
        log.info("Starting to Turn to Destination");
        Optional<LocalEndpointStatus> toEP = handshakeEndpointInfo.getHandshakeEP(currentRequest.getCapabilityIdTo());
        toEP.ifPresent(ep -> {
            currentProcessStep = InternalProcess.TURNING_DEST;
            publishRequest(new TurnRequest(EndpointToDirectionMapper.mapEndpointToDirection(toEP.get())));    //Turn to destination
        });

    }

    private void loadConveyorFromSource() {
        currentProcessStep = InternalProcess.CONVEYING_SOURCE;
        publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.LOAD));
    }

    private void unloadConveyorAtDestination() {
        currentProcessStep = InternalProcess.CONVEYING_DEST;
        publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.UNLOAD));
    }

    private void reset() {
        if (stateMachine.getState().equals(BasicMachineStates.STOPPED)
                || stateMachine.getState().equals(BasicMachineStates.COMPLETE)) {
            fireStateMachineTrigger(TransportCoordinatorTriggers.DO_RESET);
            publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.RESET));
            publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Reset));
        }
    }

    private void stop() {
        fireStateMachineTrigger(TransportCoordinatorTriggers.DO_STOPPING);
        publishRequest(new ConveyorRequest(ConveyorRequest.ConveyorRequests.STOP));
        publishRequest(new TurntableRequest(BasicMachineRequests.SimpleMessageTypes.Stop));
        handshakeEndpointInfo.tellAllEPsToStop();
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