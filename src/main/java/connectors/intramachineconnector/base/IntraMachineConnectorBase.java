package connectors.intramachineconnector.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.notifications.ServerHandshakeEndpointStatusNotification;
import msg.requests.TurntableRequest;
import msg.requests.ClientHandshakeRequest;
import msg.requests.TurnRequest;
import msg.requests.ServerHandshakeRequest;
import msg.notifications.ClientHandshakeStatusNotification;
import msg.notifications.ClientHandshakeNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.ConveyorRequest;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.ServerHandshakeNotification;
import msg.notifications.TurntableStatusUpdateNotification;

public class IntraMachineConnectorBase extends C2Connector {

    private String id = "_AhJAkeMxEDiawdooqVeh0w";

    @Inject
    public IntraMachineConnectorBase(ActorRef handshakeconnector){
        super(handshakeconnector);
    }

    protected void addSupportedMessagesTopDomain(){
		supportedMessagesTop.add(ServerHandshakeRequest.class);
		supportedMessagesTop.add(ClientHandshakeNotification.class);
		supportedMessagesTop.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(ConveyorRequest.class);
		supportedMessagesTop.add(ConveyorStatusUpdateNotification.class);
		supportedMessagesTop.add(ServerHandshakeNotification.class);
		supportedMessagesTop.add(ServerHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(TurntableRequest.class);
		supportedMessagesTop.add(TurntableStatusUpdateNotification.class);
		supportedMessagesTop.add(ClientHandshakeRequest.class);
		supportedMessagesTop.add(TurnRequest.class);
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(ServerHandshakeRequest.class);
		supportedMessagesBottom.add(ClientHandshakeNotification.class);
		supportedMessagesBottom.add(MachineStatusUpdateNotification.class);
		supportedMessagesBottom.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesBottom.add(ConveyorRequest.class);
		supportedMessagesBottom.add(ConveyorStatusUpdateNotification.class);
		supportedMessagesBottom.add(ServerHandshakeNotification.class);
		supportedMessagesBottom.add(ServerHandshakeEndpointStatusNotification.class);
		supportedMessagesBottom.add(TurntableRequest.class);
		supportedMessagesBottom.add(ClientHandshakeRequest.class);
		supportedMessagesBottom.add(TurntableStatusUpdateNotification.class);
		supportedMessagesBottom.add(TurnRequest.class);
    }

}