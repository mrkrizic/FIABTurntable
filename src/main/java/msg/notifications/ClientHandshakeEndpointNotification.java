package msg.notifications;

import c2akka.c2messages.C2Notification;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.handshake.actor.LocalEndpointStatus;

public class ClientHandshakeEndpointNotification extends C2Notification {

    private LocalEndpointStatus.LocalClientEndpointStatus status;

    public ClientHandshakeEndpointNotification(LocalEndpointStatus.LocalClientEndpointStatus status) {
        this.status = status;
    }

    public LocalEndpointStatus.LocalClientEndpointStatus getStatus() {
        return status;
    }

}
