package connectors.intramachineconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.conveyoractor.stateMachine.ConveyorStates;
import components.turningactor.stateMachine.TurningStates;
import fiab.core.capabilities.OPCUABasicMachineBrowsenames;
import fiab.core.capabilities.meta.OPCUACapabilitiesAndWiringInfoBrowsenames;
import fiab.opcua.server.OPCUABase;
import modules.opcua.OpcUaWrapper;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.TurntableStatusUpdateNotification;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class IntraMachineConnector extends C2Connector {

    private final OpcUaWrapper wrapper;
    private UaVariableNode conveyorStatus = null;
    private UaVariableNode turningStatus = null;
    private OPCUABase opcUaBase;

    @Inject
    public IntraMachineConnector(@Named("HandshakeConnector") ActorRef handshakeconnector,
                                 OpcUaWrapper wrapper) {
        super(handshakeconnector);
        this.wrapper = wrapper;
    }

    @NotificationHandler
    public void handleTurntableStatusUpdate(TurntableStatusUpdateNotification notification) {
        if (turningStatus == null) {
            addTurningFuCapability();
        } else {
            turningStatus.setValue(new DataValue(new Variant(notification.getState().name())));
        }
    }

    @NotificationHandler
    public void handleConveyorStatusUpdate(ConveyorStatusUpdateNotification notification) {
        if (conveyorStatus == null) {
            addConveyorFuCapability();
        } else {
            conveyorStatus.setValue(new DataValue(new Variant(notification.getConveyorState().name())));
        }
    }

    private void addConveyorFuCapability() {
        String path = wrapper.getMachinePrefix() + "CONVEYOR_FU";
        opcUaBase = wrapper.getOpcUaBase();
        UaFolderNode folderNode = opcUaBase.generateFolder(wrapper.getTurntableRoot(),
                wrapper.getMachinePrefix(), "CONVEYOR_FU");
        conveyorStatus = opcUaBase.generateStringVariableNode(folderNode, path, OPCUABasicMachineBrowsenames.STATE_VAR_NAME, ConveyorStates.STOPPED);
        UaFolderNode capabilitiesFolder = opcUaBase.generateFolder(folderNode, path, OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES);
        path = path + "/" + OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES;
        UaFolderNode capability1 = opcUaBase.generateFolder(capabilitiesFolder, path,
                "CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITY);

        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.TYPE,
                "http://factory-in-a-box.fiab/capabilities/transport/conveying");
        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ID,
                "DefaultConveyingCapability");
        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE,
                OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE_VALUE_PROVIDED);

    }

    private void addTurningFuCapability() {
        String path = wrapper.getMachinePrefix() + "TURNING_FU";
        opcUaBase = wrapper.getOpcUaBase();
        UaFolderNode folderNode = opcUaBase.generateFolder(wrapper.getTurntableRoot(),
                wrapper.getMachinePrefix(), "TURNING_FU");
        turningStatus = opcUaBase.generateStringVariableNode(folderNode, path, OPCUABasicMachineBrowsenames.STATE_VAR_NAME, TurningStates.STOPPED);
        UaFolderNode capabilitiesFolder = opcUaBase.generateFolder(folderNode, path, OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES);
        path = path + "/" + OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITIES;
        UaFolderNode capability1 = opcUaBase.generateFolder(capabilitiesFolder, path,
                "CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.CAPABILITY);

        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.TYPE,
                "http://factory-in-a-box.fiab/capabilities/transport/conveying");
        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ID,
                "DefaultConveyingCapability");
        opcUaBase.generateStringVariableNode(capability1, path + "/CAPABILITY", OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE,
                OPCUACapabilitiesAndWiringInfoBrowsenames.ROLE_VALUE_PROVIDED);
    }


}