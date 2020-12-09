package msg.requests;

import c2akka.c2messages.C2Request;

public class TurnRequest extends C2Request {
    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    private final Direction target;

    public TurnRequest(Direction target) {
        this.target = target;
    }

    public Direction getTarget() {
        return target;
    }
}