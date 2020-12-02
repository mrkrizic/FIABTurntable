package connectors.opcuaclientnorthconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fiab.core.capabilities.wiring.WiringInfo;
import fiab.handshake.fu.HandshakeFU;
import fiab.handshake.fu.client.ClientSideHandshakeFU;

public class OpcUaClientNorthConnector extends C2Connector {

    @Inject
    public OpcUaClientNorthConnector(){
        super();
        //HandshakeFU handshakeFU = new ClientSideHandshakeFU()
    }
}