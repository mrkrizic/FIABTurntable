package modules;

import akka.ConfigurationException;
import akka.actor.ActorSystem;
import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import configurations.ServerConfig;
import configurations.ServerInfo;
import connectors.opcuaconnector.OpcUaConnector;
import connectors.intramachineconnector.IntraMachineConnector;
import connectors.handshakeconnector.HandshakeConnector;
import connectors.opcuaclientnorthconnector.OpcUaClientNorthConnector;
import connectors.opcuaclienteastconnector.OpcUaClientEastConnector;
import connectors.opcuaclientsouthconnector.OpcUaClientSouthConnector;
import connectors.opcuaclientwestconnector.OpcUaClientWestConnector;
import components.transportmodulecoordinatoractor.impl.TransportModuleCoordinatorActorImpl;
import components.conveyoractor.impl.ConveyorActorImpl;
import components.turningactor.impl.TurningActorImpl;
import components.clienthandshakenorthactor.impl.ClientHandshakeNorthActorImpl;
import components.serverhandshakeeastactor.impl.ServerHandshakeEastActorImpl;
import components.handshakeactor.impl.HandshakeActorImpl;
import components.clienthandshakeeastactor.impl.ClientHandshakeEastActorImpl;
import components.clienthandshakesouthactor.impl.ClientHandshakeSouthActorImpl;
import components.clienthandshakewestactor.impl.ClientHandshakeWestActorImpl;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import hardware.ConveyorHardware;
import hardware.ConveyorHardwareConfig;
import hardware.TurningHardware;
import hardware.TurningHardwareConfig;
import modules.opcua.OpcUaWrapper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;

import java.util.Optional;

public class FIABTurntableModule extends ApplicationProviderModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(actorSystem);
        bindSingletonActor(actorSystem, C2EmptyConnector.class, "no-connector");
        bindSingletonActor(actorSystem, OpcUaConnector.class, "OpcUaConnector");
        bindSingletonActor(actorSystem, IntraMachineConnector.class, "IntraMachineConnector");
        bindSingletonActor(actorSystem, HandshakeConnector.class, "HandshakeConnector");
        bindSingletonActor(actorSystem, OpcUaClientNorthConnector.class, "OpcUaClientNorthConnector");
        bindSingletonActor(actorSystem, OpcUaClientEastConnector.class, "OpcUaClientEastConnector");
        bindSingletonActor(actorSystem, OpcUaClientSouthConnector.class, "OpcUaClientSouthConnector");
        bindSingletonActor(actorSystem, OpcUaClientWestConnector.class, "OpcUaClientWestConnector");

        bindActor(actorSystem, TransportModuleCoordinatorActorImpl.class, "TransportModuleCoordinatorActor");
        bindActor(actorSystem, ConveyorActorImpl.class, "ConveyorActor");
        bindActor(actorSystem, TurningActorImpl.class, "TurningActor");
        bindActor(actorSystem, ClientHandshakeNorthActorImpl.class, "ClientHandshakeNorthActor");
        bindActor(actorSystem, ServerHandshakeEastActorImpl.class, "ServerHandshakeEastActor");
        bindActor(actorSystem, HandshakeActorImpl.class, "HandshakeActor");
        bindActor(actorSystem, ClientHandshakeEastActorImpl.class, "ClientHandshakeEastActor");
        bindActor(actorSystem, ClientHandshakeSouthActorImpl.class, "ClientHandshakeSouthActor");
        bindActor(actorSystem, ClientHandshakeWestActorImpl.class, "ClientHandshakeWestActor");

    }

    @Provides
    @Singleton
    protected OpcUaWrapper provideOpcUaBase() throws Exception {
        Optional<ServerInfo> serverConfig = ServerConfig.loadServerConfigFromFileSystem();
        ServerInfo serverInfo;
        if (serverConfig.isPresent()) {
            serverInfo = serverConfig.get();
        }else {
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