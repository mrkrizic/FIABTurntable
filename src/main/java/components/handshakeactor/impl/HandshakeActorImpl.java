package components.handshakeactor.impl;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.handshakeactor.HandshakeActorBase;


public class HandshakeActorImpl extends HandshakeActorBase {

    @Inject
    public HandshakeActorImpl(@Named("HandshakeConnector") ActorRef connectorTopDomain,
                             @Named("IntraMachineConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }





}