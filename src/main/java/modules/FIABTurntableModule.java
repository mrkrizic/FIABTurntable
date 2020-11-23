package modules;

import akka.actor.ActorSystem;
import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2bricks.c2connector.C2EmptyConnector;
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

}