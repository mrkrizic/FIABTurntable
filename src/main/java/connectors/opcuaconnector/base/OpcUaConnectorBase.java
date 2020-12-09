package connectors.opcuaconnector.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaTransportRequest;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.OpcUaRequest;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class OpcUaConnectorBase extends C2Connector {

    private String id = "_3zi-UeMwEDiawdooqVeh0w";

    @Inject
    public OpcUaConnectorBase(){
        super();
    }

    protected void addSupportedMessagesTopDomain(){
		supportedMessagesTop.add(OpcUaTransportRequest.class);
		supportedMessagesTop.add(MachineStatusUpdateNotification.class);
		supportedMessagesTop.add(OpcUaRequest.class);
    }

    protected void addSupportedMessagesBottomDomain(){
    }

}