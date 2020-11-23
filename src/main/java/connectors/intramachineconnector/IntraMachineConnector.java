package connectors.intramachineconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class IntraMachineConnector extends C2Connector {

    @Inject
    public IntraMachineConnector(@Named("HandshakeConnector")ActorRef handshakeconnector){
        super(handshakeconnector);
    }
}