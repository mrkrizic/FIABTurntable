package msg.requests;

import c2akka.c2messages.C2Request;
import fiab.core.capabilities.handshake.HandshakeCapability;

public class OpcUaRequest extends C2Request {

    private final HandshakeCapability.ServerMessageTypes msg;

    public OpcUaRequest(HandshakeCapability.ServerMessageTypes msg) {
        this.msg = msg;
    }

    public HandshakeCapability.ServerMessageTypes getMsg() {
        return msg;
    }
}