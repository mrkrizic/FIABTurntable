package msg.requests;

import c2akka.c2messages.C2Request;

public class InternalTransportModuleRequest extends C2Request {
    private final String capIdFrom;
    private final String capIdTo;
    private final String orderId;
    private final String reqId;

    public InternalTransportModuleRequest(String capIdFrom, String capIdTo, String orderId, String reqId) {

        this.capIdFrom = capIdFrom;
        this.capIdTo = capIdTo;
        this.orderId = orderId;
        this.reqId = reqId;
    }

    public String getCapIdFrom() {
        return capIdFrom;
    }

    public String getCapIdTo() {
        return capIdTo;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getReqId() {
        return reqId;
    }
}