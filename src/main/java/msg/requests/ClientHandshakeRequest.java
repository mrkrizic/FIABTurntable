package msg.requests;

import c2akka.c2messages.C2Request;
import fiab.core.capabilities.handshake.HandshakeCapability;

public class ClientHandshakeRequest extends C2Request {

    private final String capabilityId;
    private final HandshakeCapability.ClientMessageTypes type;

    public ClientHandshakeRequest(String capabilityId, HandshakeCapability.ClientMessageTypes type) {
        this.capabilityId = capabilityId;
        this.type = type;
    }

    public HandshakeCapability.ClientMessageTypes getType() {
        return type;
    }

    public String getCapabilityId() {
        return capabilityId;
    }
}