package msg.notifications;

import c2akka.c2messages.C2Notification;
import fiab.handshake.actor.LocalEndpointStatus;

public class ClientHandshakeEndpointStatusNotification extends C2Notification {
    private LocalEndpointStatus.LocalClientEndpointStatus status;

    public ClientHandshakeEndpointStatusNotification(LocalEndpointStatus.LocalClientEndpointStatus status) {
        this.status = status;
    }

    public LocalEndpointStatus.LocalClientEndpointStatus getStatus() {
        return status;
    }
}