package components.serverhandshakeeastactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.serverhandshakeeastactor.ServerHandshakeEastActorBase;


public class ServerHandshakeEastActorImpl extends ServerHandshakeEastActorBase {

    @Inject
    public ServerHandshakeEastActorImpl(@Named("no-connector") ActorRef connectorTopDomain,
                             @Named("HandshakeConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }





}