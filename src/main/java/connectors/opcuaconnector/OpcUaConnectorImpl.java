package connectors.opcuaconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import connectors.opcuaconnector.base.OpcUaConnectorBase;
import connectors.opcuaconnector.methods.ResetMethod;
import connectors.opcuaconnector.methods.StopMethod;
import connectors.opcuaconnector.methods.TransportRequest;
import fiab.core.capabilities.BasicMachineStates;
import fiab.core.capabilities.OPCUABasicMachineBrowsenames;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.meta.OPCUACapabilitiesAndWiringInfoBrowsenames;
import fiab.core.capabilities.transport.TransportModuleCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.opcua.server.OPCUABase;
import modules.opcua.OpcUaWrapper;
import msg.notifications.MachineStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;
import msg.requests.OpcUaTransportRequest;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class OpcUaConnectorImpl extends OpcUaConnectorBase {

    private final OpcUaWrapper opcUaWrapper;
    private final OPCUABase opcUaBase;
    private String currentState = "";
    private UaVariableNode statusVariableNode;
    private boolean isWaitingForResponse;
    private ActorRef transportClient;

    @Inject
    public OpcUaConnectorImpl(OpcUaWrapper opcUaWrapper) {
        super();
        this.isWaitingForResponse = false;
        this.opcUaWrapper = opcUaWrapper;
        this.opcUaBase = opcUaWrapper.getOpcUaBase();
        Thread serverThread = new Thread(opcUaBase);
        serverThread.start();
        initServer();
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(OpcUaTransportRequest.class, request -> {
                            isWaitingForResponse = true;
                            transportClient = sender();
                            publishRequest(request);
                        })
                        .match(HandshakeCapability.ServerMessageTypes.class, msg -> {
                            log.info("Received ServerHandshakeMessage: " + msg);
                            publishRequest(new OpcUaRequest(msg));
                        })
                        .matchAny(msg -> log.info("Received " + msg.getClass()))
                        .build());
    }

    private void initServer() {
        setTurntableFuOpcUaNodes(opcUaWrapper.getTurntableRoot()
                , opcUaWrapper.getMachinePrefix());
        setupTurntableCapabilities(opcUaWrapper.getOpcUaBase(), opcUaWrapper.getTurntableRoot(), opcUaWrapper.getMachinePrefix());
    }

    @NotificationHandler
    public void handleMachineStatusUpdateNotification(MachineStatusUpdateNotification notification) {
        if (isWaitingForResponse) {
            transportClient.tell(notification, self());
            transportClient = null;     //We don't need the ref anymore after we replied
        }
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

    private void setTurntableFuOpcUaNodes(UaFolderNode ttNode, String coordinatorPrefix) {
        UaMethodNode n1 = opcUaBase.createPartialMethodNode(coordinatorPrefix,
                TurntableModuleWellknownCapabilityIdentifiers.SimpleMessageTypes.Reset.toString(), "Requests reset");
        opcUaBase.addMethodNode(ttNode, n1, new ResetMethod(n1, self()));

        UaMethodNode n2 = opcUaBase.createPartialMethodNode(coordinatorPrefix,
                TurntableModuleWellknownCapabilityIdentifiers.SimpleMessageTypes.Stop.toString(), "Requests stop");
        opcUaBase.addMethodNode(ttNode, n2, new StopMethod(n2, self()));

        UaMethodNode n3 = opcUaBase.createPartialMethodNode(coordinatorPrefix,
                TransportModuleCapability.OPCUA_TRANSPORT_REQUEST, "Requests transport");
        opcUaBase.addMethodNode(ttNode, n3, new TransportRequest(n3, self()));

        statusVariableNode = opcUaBase.generateStringVariableNode(ttNode, coordinatorPrefix,
                OPCUABasicMachineBrowsenames.STATE_VAR_NAME, BasicMachineStates.UNKNOWN);
    }

    private void setupTurntableCapabilities(OPCUABase opcuaBase, UaFolderNode ttNode, String path) {
        // add capabilities
        UaFolderNode capabilitiesFolder = opcuaBase.generateFolder(ttNode, path, new String(OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES));
        path = path + "/" + OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES;
        UaFolderNode capability1 = opcuaBase.generateFolder(capabilitiesFolder, path,
                "CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITY);
        opcuaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.TYPE,
                TransportModuleCapability.TRANSPORT_CAPABILITY_URI);
        opcuaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ID,
                "DefaultTurntableCapabilityInstance");
        opcuaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE,
                OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE_VALUE_PROVIDED);
    }
}