package components.clienthandshakewestactor.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.notifications.ClientHandshakeNotification;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.requests.ClientHandshakeRequest;


public abstract class ClientHandshakeWestActorBase extends C2Component {

    private String id = "_WJIyQOx9EDixHZoJd379eQ";

    public ClientHandshakeWestActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest);




    protected void addSupportedMessagesTopDomain(){
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(ClientHandshakeNotification.class);
		supportedMessagesBottom.add(ClientHandshakeEndpointStatusNotification.class);
		supportedMessagesBottom.add(ClientHandshakeRequest.class);
    }

}