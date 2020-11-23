package components.serverhandshakeeastactor;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;


public abstract class ServerHandshakeEastActorBase extends C2Component {

    private String id = "_DUTPQeMyEDiawdooqVeh0w";

    public ServerHandshakeEastActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }





}