package components.serverhandshakeeastactor.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.requests.ServerHandshakeRequest;
import msg.notifications.ServerHandshakeNotification;
import msg.notifications.ServerHandshakeEndpointStatusNotification;


public abstract class ServerHandshakeEastActorBase extends C2Component {

    private String id = "_DUTPQeMyEDiawdooqVeh0w";

    public ServerHandshakeEastActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleServerHandshakeRequest(ServerHandshakeRequest serverHandshakeRequest);




    protected void addSupportedMessagesTopDomain(){
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(ServerHandshakeRequest.class);
		supportedMessagesBottom.add(ServerHandshakeNotification.class);
		supportedMessagesBottom.add(ServerHandshakeEndpointStatusNotification.class);
    }

}