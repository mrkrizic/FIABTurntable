package connectors.opcuaconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import connectors.opcuaconnector.methods.TransportRequest;
import fiab.core.capabilities.BasicMachineStates;
import fiab.core.capabilities.OPCUABasicMachineBrowsenames;
import fiab.core.capabilities.basicmachine.BasicMachineRequests;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.transport.TransportModuleCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.handshake.fu.server.methods.Reset;
import fiab.handshake.fu.server.methods.Stop;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class OpcUaConnector extends C2Connector {

    private OPCUABase opcuaBase;
    private final String machineName = "Turntable";
    private String currentState = "";
    private UaVariableNode statusVariableNode;

    @Inject
    public OpcUaConnector() {
        super();
        try {
            NonEncryptionBaseOpcUaServer server = new NonEncryptionBaseOpcUaServer(0, machineName);
            OpcUaServer opcUaServer = server.getServer();
            this.opcuaBase = new OPCUABase(opcUaServer, "urn:factory-in-a-box", machineName);
            Thread serverThread = new Thread(opcuaBase::run);
            serverThread.start();
            initServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(HandshakeCapability.ServerMessageTypes.class, msg -> {
                            log.info("Received ServerHandshakeMessage: " + msg);
                            publishRequest(new OpcUaRequest(msg));
                        })
                        .matchAny(msg -> log.info("Received " + msg.getClass()))
                        .build());
    }

    private void initServer() {
        UaFolderNode root = opcuaBase.prepareRootNode();
        UaFolderNode ttNode = opcuaBase.generateFolder(root, machineName, "Turntable_FU");
        String coordinatorPrefix = machineName + "/" + "Turntable_FU";
        setTurntableFuOpcUaNodes(ttNode, coordinatorPrefix);
    }

    private void setTurntableFuOpcUaNodes(UaFolderNode ttNode, String coordinatorPrefix) {
        UaMethodNode n1 = opcuaBase.createPartialMethodNode(coordinatorPrefix, TurntableModuleWellknownCapabilityIdentifiers.SimpleMessageTypes.Reset.toString(), "Requests reset");
        opcuaBase.addMethodNode(ttNode, n1, new Reset(n1, self()));
        UaMethodNode n2 = opcuaBase.createPartialMethodNode(coordinatorPrefix, TurntableModuleWellknownCapabilityIdentifiers.SimpleMessageTypes.Stop.toString(), "Requests stop");
        opcuaBase.addMethodNode(ttNode, n2, new Stop(n2, self()));
        UaMethodNode n3 = opcuaBase.createPartialMethodNode(coordinatorPrefix, TransportModuleCapability.OPCUA_TRANSPORT_REQUEST, "Requests transport");
        opcuaBase.addMethodNode(ttNode, n3, new TransportRequest(n3, self()));
        statusVariableNode = opcuaBase.generateStringVariableNode(ttNode, coordinatorPrefix, OPCUABasicMachineBrowsenames.STATE_VAR_NAME, BasicMachineStates.UNKNOWN);
    }

    @NotificationHandler
    public void handleMachineStatusUpdateNotification(MachineStatusUpdateNotification notification) {
        this.currentState = notification.getState();
        log.info("Received Updated State: " + notification.getState());
        statusVariableNode.setValue(new DataValue(new Variant(currentState)));
    }

    @RequestHandler
    public void handleInternalTransportModuleRequest(InternalTransportModuleRequest request) {
        publishRequest(request);
        sender().tell(currentState, self());
        log.info("Received Internal Transport Module Request");
    }
}