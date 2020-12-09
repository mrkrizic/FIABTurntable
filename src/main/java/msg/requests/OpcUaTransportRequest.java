package msg.requests;

import c2akka.c2messages.C2Request;

public class OpcUaTransportRequest extends C2Request {

    private final String capabilityIdFrom;
    private final String capabilityIdTo;
    private final String orderId;

    public OpcUaTransportRequest(String capabilityIdFrom, String capabilityIdTo, String orderId) {
        this.capabilityIdFrom = capabilityIdFrom;
        this.capabilityIdTo = capabilityIdTo;
        this.orderId = orderId;
    }

    public String getCapabilityIdFrom() {
        return capabilityIdFrom;
    }

    public String getCapabilityIdTo() {
        return capabilityIdTo;
    }

    public String getOrderId() {
        return orderId;
    }
}