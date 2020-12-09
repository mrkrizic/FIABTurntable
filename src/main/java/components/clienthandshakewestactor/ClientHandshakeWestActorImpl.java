package components.clienthandshakewestactor;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import components.clienthandshakewestactor.base.ClientHandshakeWestActorBase;
import configurations.WiringUtils;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.core.capabilities.wiring.WiringInfo;
import fiab.handshake.actor.LocalEndpointStatus;
import fiab.handshake.fu.HandshakeFU;
import fiab.handshake.fu.client.ClientSideHandshakeFU;
import modules.opcua.OpcUaWrapper;
import msg.notifications.ClientHandshakeEndpointStatusNotification;
import msg.notifications.ClientHandshakeNotification;
import msg.requests.ClientHandshakeRequest;

import java.util.HashMap;
import java.util.Optional;

public class ClientHandshakeWestActorImpl extends ClientHandshakeWestActorBase {

    private final boolean EXPOSE_INTERNAL_CONTROLS = false;

    private final OpcUaWrapper opcUaWrapper;
    private final HandshakeFU handshakeFU;
    private final String capabilityId;

    @Inject
    public ClientHandshakeWestActorImpl(@Named("NoConnector") ActorRef connectorTopDomain,
                                        @Named("HandshakeConnector") ActorRef connectorBottomDomain,
                                        OpcUaWrapper opcUaWrapper) {
        super(connectorTopDomain, connectorBottomDomain);
        this.opcUaWrapper = opcUaWrapper;
        this.capabilityId = TurntableModuleWellknownCapabilityIdentifiers.TRANSPORT_MODULE_WEST_CLIENT;
        this.handshakeFU = new ClientSideHandshakeFU(opcUaWrapper.getOpcUaBase(), opcUaWrapper.getTurntableRoot(),
                opcUaWrapper.getMachinePrefix(), self(), context(), capabilityId, false,
                EXPOSE_INTERNAL_CONTROLS);
        loadWiringFromFile();
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(receiveBuilder()
                .match(LocalEndpointStatus.LocalClientEndpointStatus.class, status -> {
                    log.info("Received Client Endpoint Status: " + status.getState());
                    publishNotification(new ClientHandshakeEndpointStatusNotification(status));
                })
                .match(HandshakeCapability.ClientSideStates.class, state -> {
                    log.info("Received Client Side State: " + state.name());
                    publishNotification(new ClientHandshakeNotification(capabilityId, state));
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg.toString()))
                .build());
    }


    @RequestHandler
    public void handleClientHandshakeRequest(ClientHandshakeRequest clientHandshakeRequest) {
        if(clientHandshakeRequest.getCapabilityId().equals(capabilityId)) {
            log.debug("Received Client Handshake Request " + clientHandshakeRequest);
            handshakeFU.getFUActor().tell(clientHandshakeRequest.getType(), self());
        }
    }

    private void loadWiringFromFile() {
        Optional<HashMap<String, WiringInfo>> optInfo = WiringUtils.loadWiringInfoFromFileSystem(opcUaWrapper.getMachineName());
        optInfo.ifPresent(info -> {
            info.values().stream()
                    .filter(wiringInfo -> wiringInfo.getLocalCapabilityId().equals(capabilityId))
                    .forEach(wi -> {
                        try {
                            handshakeFU.provideWiringInfo(wi);
                        } catch (Exception e) {
                            log.warning("Error applying wiring info " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
        });
    }

}