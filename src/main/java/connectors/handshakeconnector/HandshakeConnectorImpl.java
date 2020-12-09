package connectors.handshakeconnector;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2connector.C2Connector;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import connectors.handshakeconnector.base.HandshakeConnectorBase;
import msg.notifications.ClientHandshakeEndpointStatusNotification;

public class HandshakeConnectorImpl extends HandshakeConnectorBase {

    @Inject
    public HandshakeConnectorImpl() {
        super();
    }

}