package msg.requests;

import c2akka.c2messages.C2Request;

public class ConveyorRequest extends C2Request {
    public enum ConveyorRequests{
        STOP, RESET, LOAD, UNLOAD
    }

    private final ConveyorRequests conveyorRequest;

    public ConveyorRequest(ConveyorRequests request) {
        this.conveyorRequest = request;
    }

    public ConveyorRequests getConveyorRequest() {
        return conveyorRequest;
    }
}