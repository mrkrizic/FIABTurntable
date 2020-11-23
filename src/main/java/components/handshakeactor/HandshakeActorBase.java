package components.handshakeactor;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;


public abstract class HandshakeActorBase extends C2Component {

    private String id = "_SMIUQeM0EDiawdooqVeh0w";

    public HandshakeActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }





}