package components.clienthandshakeeastactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.clienthandshakeeastactor.ClientHandshakeEastActorBase;
import msg.requests.ClientHandshakeRequest;

public class ClientHandshakeEastActorImpl extends ClientHandshakeEastActorBase {

    @Inject
    public ClientHandshakeEastActorImpl(@Named("OpcUaClientEastConnector") ActorRef connectorTopDomain,
                             @Named("HandshakeConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest) {
        //TODO implement method stub
    }



}