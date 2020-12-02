package connectors.opcuaclienteastconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fiab.handshake.actor.ClientHandshakeActor;
import fiab.handshake.fu.client.ClientSideHandshakeFU;
import modules.opcua.OpcUaWrapper;

public class OpcUaClientEastConnector extends C2Connector {

    @Inject
    public OpcUaClientEastConnector(){
        super();
    }
}