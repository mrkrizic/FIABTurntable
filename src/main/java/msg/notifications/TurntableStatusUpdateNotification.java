package msg.notifications;

import c2akka.c2messages.C2Notification;
import components.turningactor.stateMachine.TurningStates;

public class TurntableStatusUpdateNotification extends C2Notification {
    private final TurningStates state;

    public TurntableStatusUpdateNotification(TurningStates state) {
        this.state = state;
    }

    public TurningStates getState() {
        return state;
    }
}