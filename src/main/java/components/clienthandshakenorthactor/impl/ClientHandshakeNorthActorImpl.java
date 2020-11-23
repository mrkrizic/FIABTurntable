package components.clienthandshakenorthactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.clienthandshakenorthactor.ClientHandshakeNorthActorBase;
import msg.requests.ClientHandshakeRequest;

public class ClientHandshakeNorthActorImpl extends ClientHandshakeNorthActorBase {

    @Inject
    public ClientHandshakeNorthActorImpl(@Named("OpcUaClientNorthConnector") ActorRef connectorTopDomain,
                             @Named("HandshakeConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest) {
        //TODO implement method stub
    }



}