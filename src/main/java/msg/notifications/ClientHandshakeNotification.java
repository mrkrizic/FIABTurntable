package msg.notifications;

import c2akka.c2messages.C2Notification;
import fiab.core.capabilities.handshake.HandshakeCapability;

public class ClientHandshakeNotification extends C2Notification {
    private final String capabilityId;
    private final HandshakeCapability.ClientSideStates state;

    public ClientHandshakeNotification(String capabilityId, HandshakeCapability.ClientSideStates state) {
        this.capabilityId = capabilityId;
        this.state = state;
    }

    public HandshakeCapability.ClientSideStates getState() {
        return state;
    }

    public String getCapabilityId() {
        return capabilityId;
    }
}