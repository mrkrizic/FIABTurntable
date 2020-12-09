package msg.notifications;

import c2akka.c2messages.C2Notification;

public class MachineStatusUpdateNotification extends C2Notification {
    private final String state;

    public MachineStatusUpdateNotification(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}