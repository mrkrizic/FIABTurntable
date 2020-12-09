package components.serverhandshakeeastactor;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.serverhandshakeeastactor.base.ServerHandshakeEastActorBase;
import msg.requests.ServerHandshakeRequest;

public class ServerHandshakeEastActorImpl extends ServerHandshakeEastActorBase {

    @Inject
    public ServerHandshakeEastActorImpl(@Named("NoConnector") ActorRef connectorTopDomain,
                             @Named("HandshakeConnector") ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public void handleServerHandshakeRequest(ServerHandshakeRequest serverHandshakeRequest) {
        //TODO implement method stub
    }




}