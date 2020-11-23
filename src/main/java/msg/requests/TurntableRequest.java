package msg.requests;

import c2akka.c2messages.C2Request;
import fiab.core.capabilities.BasicMachineStates;
import static fiab.core.capabilities.basicmachine.BasicMachineRequests.*;

public class TurntableRequest extends C2Request {

    private final SimpleMessageTypes message;

    public TurntableRequest(SimpleMessageTypes message) {
        this.message = message;
    }

    public SimpleMessageTypes getMessage() {
        return message;
    }
}