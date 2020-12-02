package components.transportmodulecoordinatoractor.handshake;

import akka.actor.ActorRef;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.handshake.actor.LocalEndpointStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HandshakeEndpointInfo {
    protected Map<String, LocalEndpointStatus> handshakeEPs = new HashMap<>();

    ActorRef self;

    public HandshakeEndpointInfo(ActorRef self) {
        this.self = self;
    }

    public Optional<LocalEndpointStatus> getHandshakeEP(String capabilityId) {
        if (capabilityId != null && handshakeEPs.containsKey(capabilityId))
            return Optional.ofNullable(handshakeEPs.get(capabilityId));
        else
            return Optional.empty();
    }

    public Map<String, LocalEndpointStatus> getAvailableEndpoints(){
        return handshakeEPs;
    }

    public void addOrReplace(LocalEndpointStatus les) {
        handshakeEPs.put(les.getCapabilityId(), les);
    }

    public void tellAllEPsToStop() {
        handshakeEPs.values().stream()
                .forEach(les -> {
                    if (les.isProvidedCapability()) {                    // if server use server msg
                        les.getActor().tell(HandshakeCapability.ServerMessageTypes.Stop, self);
                    } else {
                        les.getActor().tell(HandshakeCapability.ClientMessageTypes.Stop, self);
                    }
                });
    }

}

