package mockServer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2architecture.C2ArchitectureBuilder;
import components.handshakeactor.impl.HandshakeActorImpl;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.meta.OPCUACapabilitiesAndWiringInfoBrowsenames;
import fiab.handshake.actor.LocalEndpointStatus;
import fiab.handshake.fu.HandshakeFU;
import fiab.handshake.fu.server.ServerSideHandshakeFU;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import modules.FIABTurntableModule;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import runner.FIABTurntableApplicationRunner;

public class FIABMockServers {

    private ActorSystem system;

    public static void main(String[] args) {
       //startTTAndServers();
        startServersOnly();
    }

    public static void startTTAndServers(){
        new FIABMockServers();
        ApplicationProviderModule module = new FIABTurntableModule();
        new C2ArchitectureBuilder(FIABTurntableApplicationRunner.class, module).buildArchitectureAndRun();
    }
    private static void startServersOnly(){
        new FIABMockServers();
    }

    public FIABMockServers() {
        system = ActorSystem.create();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> system.terminate()));
        system.actorOf(Props.create(FIABMockServer.class, () -> new FIABMockServer("MachineNorth", 1, true)));
        system.actorOf(Props.create(FIABMockServer.class, () -> new FIABMockServer("MachineEast", 2, false)));
        system.actorOf(Props.create(FIABMockServer.class, () -> new FIABMockServer("MachineSouth", 3, false)));
        system.actorOf(Props.create(FIABMockServer.class, () -> new FIABMockServer("MachineWest", 4, false)));
    }

    static class FIABMockServer extends AbstractActor {
        private HandshakeFU handshakeFU;
        private boolean autoReload;

        public FIABMockServer(String name, int portOffset, boolean autoReload) {
            {
                try {
                    String machineNodePrefix = name + "/" + "Mock_FU";
                    OPCUABase opcuaBase;
                    opcuaBase = new OPCUABase(new PublicNonEncryptionBaseOpcUaServer(portOffset, name).getServer(),
                            "urn:factory-in-a-box", name);
                    UaFolderNode rootNode = opcuaBase.prepareRootNode();
                    UaFolderNode machineNode = opcuaBase.generateFolder(rootNode, name, "Mock_FU");
                    handshakeFU = new ServerSideHandshakeFU(opcuaBase, rootNode, machineNodePrefix,
                            self(), context(), "DefaultServerSideHandshake", OPCUACapabilitiesAndWiringInfoBrowsenames.IS_PROVIDED, true);
                    if(autoReload) handshakeFU.getFUActor().tell(HandshakeCapability.StateOverrideRequests.SetLoaded, ActorRef.noSender());
                    new Thread(opcuaBase).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(HandshakeCapability.ServerSideStates.class, state -> {
                        if (state.equals(HandshakeCapability.ServerSideStates.COMPLETE)
                                || state.equals(HandshakeCapability.ServerSideStates.STOPPED)) {
                            if (autoReload) {
                                handshakeFU.getFUActor().tell(HandshakeCapability.StateOverrideRequests.SetLoaded, self());
                            }
                            handshakeFU.getFUActor().tell(HandshakeCapability.ServerMessageTypes.Reset, self());
                        }
                    }).build();
        }
    }
}
