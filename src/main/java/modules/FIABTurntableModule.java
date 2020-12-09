package modules;

import akka.ConfigurationException;
import akka.actor.ActorSystem;
import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import configurations.ServerConfig;
import configurations.ServerInfo;
import connectors.opcuaconnector.OpcUaConnectorImpl;
import connectors.intramachineconnector.IntraMachineConnectorImpl;
import connectors.handshakeconnector.HandshakeConnectorImpl;
import components.transportmodulecoordinatoractor.TransportModuleCoordinatorActorImpl;
import components.conveyoractor.ConveyorActorImpl;
import components.turningactor.TurningActorImpl;
import components.clienthandshakenorthactor.ClientHandshakeNorthActorImpl;
import components.serverhandshakeeastactor.ServerHandshakeEastActorImpl;
import components.clienthandshakeeastactor.ClientHandshakeEastActorImpl;
import components.clienthandshakesouthactor.ClientHandshakeSouthActorImpl;
import components.clienthandshakewestactor.ClientHandshakeWestActorImpl;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import hardware.ConveyorHardware;
import hardware.ConveyorHardwareConfig;
import hardware.TurningHardware;
import hardware.TurningHardwareConfig;
import modules.opcua.OpcUaWrapper;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;

import java.util.Optional;

public class FIABTurntableModule extends ApplicationProviderModule {

    public FIABTurntableModule(AbstractModule... modules) {
        super(modules);
    }

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(actorSystem);
        bindSingletonActor(actorSystem, C2EmptyConnector.class, "NoConnector");
        bindSingletonActor(actorSystem, OpcUaConnectorImpl.class, "OpcUaConnector");
        bindSingletonActor(actorSystem, IntraMachineConnectorImpl.class, "IntraMachineConnector");
        bindSingletonActor(actorSystem, HandshakeConnectorImpl.class, "HandshakeConnector");

        bindActor(actorSystem, TransportModuleCoordinatorActorImpl.class, "TransportModuleCoordinatorActor");
        bindActor(actorSystem, ConveyorActorImpl.class, "ConveyorActor");
        bindActor(actorSystem, TurningActorImpl.class, "TurningActor");
        bindActor(actorSystem, ClientHandshakeNorthActorImpl.class, "ClientHandshakeNorthActor");
        bindActor(actorSystem, ServerHandshakeEastActorImpl.class, "ServerHandshakeEastActor");
        bindActor(actorSystem, ClientHandshakeEastActorImpl.class, "ClientHandshakeEastActor");
        bindActor(actorSystem, ClientHandshakeSouthActorImpl.class, "ClientHandshakeSouthActor");
        bindActor(actorSystem, ClientHandshakeWestActorImpl.class, "ClientHandshakeWestActor");
    }
}