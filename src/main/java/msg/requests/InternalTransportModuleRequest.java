package msg.requests;

import c2akka.c2messages.C2Request;

public class InternalTransportModuleRequest extends C2Request {
    private final String capIdFrom;
    private final String capIdTo;

    public InternalTransportModuleRequest(String capIdFrom, String capIdTo) {
        this.capIdFrom = capIdFrom;
        this.capIdTo = capIdTo;
    }

    public String getCapIdFrom() {
        return capIdFrom;
    }

    public String getCapIdTo() {
        return capIdTo;
    }

}