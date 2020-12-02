package connectors.handshakeconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fiab.handshake.fu.HandshakeFU;

public class HandshakeConnector extends C2Connector {

    @Inject
    public HandshakeConnector(){
        super();
    }
}