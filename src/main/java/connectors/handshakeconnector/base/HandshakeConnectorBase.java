package connectors.handshakeconnector.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import msg.notifications.ClientHandshakeStatusNotification;
import msg.requests.ServerHandshakeRequest;
import msg.notifications.ClientHandshakeNotification;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.notifications.ServerHandshakeNotification;
import msg.notifications.ServerHandshakeEndpointStatusNotification;
import msg.requests.ClientHandshakeRequest;

public class HandshakeConnectorBase extends C2Connector {

    private String id = "_nzj3EOMxEDiawdooqVeh0w";

    @Inject
    public HandshakeConnectorBase(){
        super();
    }

    protected void addSupportedMessagesTopDomain(){
		supportedMessagesTop.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(ClientHandshakeStatusNotification.class);
		supportedMessagesTop.add(ServerHandshakeRequest.class);
		supportedMessagesTop.add(ClientHandshakeNotification.class);
		supportedMessagesTop.add(ServerHandshakeNotification.class);
		supportedMessagesTop.add(ServerHandshakeEndpointStatusNotification.class);
		supportedMessagesTop.add(ClientHandshakeRequest.class);
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(ServerHandshakeRequest.class);
		supportedMessagesBottom.add(ClientHandshakeNotification.class);
		supportedMessagesBottom.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesBottom.add(ServerHandshakeNotification.class);
		supportedMessagesBottom.add(ServerHandshakeEndpointStatusNotification.class);
		supportedMessagesBottom.add(ClientHandshakeRequest.class);
    }

}