package components.clienthandshakesouthactor;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.requests.ClientHandshakeRequest;

public abstract class ClientHandshakeSouthActorBase extends C2Component {

    private String id = "_SbhRAOx9EDixHZoJd379eQ";

    public ClientHandshakeSouthActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest);



}