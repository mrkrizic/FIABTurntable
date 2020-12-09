package modules.opcua;

import fiab.opcua.server.OPCUABase;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;

public class OpcUaWrapper {

    private final OPCUABase opcUaBase;
    private final UaFolderNode opcUaRoot;
    private final UaFolderNode turntableRoot;
    private final String machineName;
    private final String machinePrefix;

    public OpcUaWrapper(OPCUABase opcUaBase, UaFolderNode opcUaRoot, UaFolderNode turntableRoot, String machineName, String machinePrefix) {
        this.opcUaBase = opcUaBase;
        this.opcUaRoot = opcUaRoot;
        this.turntableRoot = turntableRoot;
        this.machineName = machineName;
        this.machinePrefix = machinePrefix;
    }

    public OPCUABase getOpcUaBase() {
        return opcUaBase;
    }

    public UaFolderNode getOpcUaRoot() {
        return opcUaRoot;
    }

    public UaFolderNode getTurntableRoot() {
        return turntableRoot;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getMachinePrefix() {
        return machinePrefix;
    }
}
