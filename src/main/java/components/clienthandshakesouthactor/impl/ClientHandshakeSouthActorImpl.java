package components.clienthandshakesouthactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.clienthandshakesouthactor.ClientHandshakeSouthActorBase;
import msg.requests.ClientHandshakeRequest;

public class ClientHandshakeSouthActorImpl extends ClientHandshakeSouthActorBase {

    @Inject
    public ClientHandshakeSouthActorImpl(@Named("OpcUaClientEastConnector") ActorRef connectorTopDomain,
                             @Named("HandshakeConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest) {
        //TODO implement method stub
    }



}