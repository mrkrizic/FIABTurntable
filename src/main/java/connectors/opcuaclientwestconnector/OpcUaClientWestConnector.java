package connectors.opcuaclientwestconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OpcUaClientWestConnector extends C2Connector {

    @Inject
    public OpcUaClientWestConnector(){
        super();
    }
}