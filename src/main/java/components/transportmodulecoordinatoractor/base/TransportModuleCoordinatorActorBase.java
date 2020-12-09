package components.transportmodulecoordinatoractor.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.requests.OpcUaTransportRequest;
import msg.notifications.ServerHandshakeEndpointStatusNotification;
import msg.requests.TurntableRequest;
import msg.requests.ClientHandshakeRequest;
import msg.requests.TurnRequest;
import msg.requests.ServerHandshakeRequest;
import msg.notifications.MachineStatusUpdateNotification;
import msg.notifications.ClientHandshakeNotification;
import msg.requests.ConveyorRequest;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.ServerHandshakeNotification;
import msg.requests.OpcUaRequest;
import msg.notifications.TurntableStatusUpdateNotification;


public abstract class TransportModuleCoordinatorActorBase extends C2Component {

    private String id = "_r7zUQOMwEDiawdooqVeh0w";

    public TransportModuleCoordinatorActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleOpcUaTransportRequest(OpcUaTransportRequest opcUaTransportRequest);
    @RequestHandler
    public abstract void handleOpcUaRequest(OpcUaRequest opcUaRequest);


    //@NotificationHandler
    //public abstract void handleMachineStatusUpdateNotification(MachineStatusUpdateNotification machineStatusUpdateNotification);
    @NotificationHandler
    public abstract void handleClientHandshakeNotification(ClientHandshakeNotification clientHandshakeNotification);
    @NotificationHandler
    public abstract void handleClientHandshakeEndpointStatusNotification(ClientHandshakeEndpointStatusNotification clientHandshakeEndpointStatusNotification);
    @NotificationHandler
    public abstract void handleConveyorStatusUpdateNotification(ConveyorStatusUpdateNotification conveyorStatusUpdateNotification);
    @NotificationHandler
    public abstract void handleServerHandshakeNotification(ServerHandshakeNotification serverHandshakeNotification);
    @NotificationHandler
    public abstract void handleServerHandshakeEndpointStatusNotification(ServerHandshakeEndpointStatusNotification serverHandshakeEndpointStatusNotification);
    @NotificationHandler
    public abstract void handleTurntableStatusUpdateNotification(TurntableStatusUpdateNotification turntableStatusUpdateNotification);


    protected void addSupportedMessagesTopDomain(){
		supportedMessagesTop.add(ServerHandshakeRequest.class);
		supportedMessagesTop.add(MachineStatusUpdateNotification.class);
		supportedMessagesTop.add(ClientHandshakeNotification.class);
		supportedMessagesTop.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(ConveyorRequest.class);
		supportedMessagesTop.add(ConveyorStatusUpdateNotification.class);
		supportedMessagesTop.add(ServerHandshakeNotification.class);
		supportedMessagesTop.add(ServerHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(TurntableRequest.class);
		supportedMessagesTop.add(ClientHandshakeRequest.class);
		supportedMessagesTop.add(TurntableStatusUpdateNotification.class);
		supportedMessagesTop.add(TurnRequest.class);
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(OpcUaTransportRequest.class);
		supportedMessagesBottom.add(MachineStatusUpdateNotification.class);
		supportedMessagesBottom.add(OpcUaRequest.class);
    }

}