package modules;

import akka.ConfigurationException;
import akka.actor.ActorSystem;
import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import components.clienthandshakeeastactor.ClientHandshakeEastActorImpl;
import components.clienthandshakenorthactor.ClientHandshakeNorthActorImpl;
import components.clienthandshakesouthactor.ClientHandshakeSouthActorImpl;
import components.clienthandshakewestactor.ClientHandshakeWestActorImpl;
import components.conveyoractor.ConveyorActorImpl;
import components.serverhandshakeeastactor.ServerHandshakeEastActorImpl;
import components.transportmodulecoordinatoractor.TransportModuleCoordinatorActorImpl;
import components.turningactor.TurningActorImpl;
import configurations.ServerConfig;
import configurations.ServerInfo;
import connectors.handshakeconnector.HandshakeConnectorImpl;
import connectors.intramachineconnector.IntraMachineConnectorImpl;
import connectors.opcuaconnector.OpcUaConnectorImpl;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import hardware.ConveyorHardware;
import hardware.ConveyorHardwareConfig;
import hardware.TurningHardware;
import hardware.TurningHardwareConfig;
import modules.opcua.OpcUaWrapper;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;

import java.util.Optional;

public class FIABModule extends ApplicationProviderModule {
    @Override
    protected void configure() {
       //We just have providers for non actors. Add actors here
    }

    @Provides
    @Singleton
    protected OpcUaWrapper provideOpcUaBase() throws Exception {
        Optional<ServerInfo> serverConfig = ServerConfig.loadServerConfigFromFileSystem();
        ServerInfo serverInfo;
        if (serverConfig.isPresent()) {
            serverInfo = serverConfig.get();
            serverInfo.setName("FIABLocalTurntable");
        } else {
            throw new ConfigurationException("Could not find ServerInfo! ");
        }
        String machineName = serverInfo.getName();
        String machineNodePrefix = machineName + "/" + "Turntable_FU";
        OPCUABase opcuaBase = new OPCUABase(new PublicNonEncryptionBaseOpcUaServer(serverInfo.getPortOffset(), machineName).getServer(),
                "urn:factory-in-a-box", machineName);
        UaFolderNode rootNode = opcuaBase.prepareRootNode();
        UaFolderNode ttNode = opcuaBase.generateFolder(rootNode, machineName, "Turntable_FU");
        return new OpcUaWrapper(opcuaBase, rootNode, ttNode, machineName, machineNodePrefix);
    }

    @Provides
    @Singleton
    protected TurningHardware provideTurningHardware() {
        return new TurningHardwareConfig().getTurningHardware();
    }

    @Provides
    @Singleton
    protected ConveyorHardware provideConveyorHardware() {
        return new ConveyorHardwareConfig().getConveyorHardware();
    }
}
