package components.turningactor.base;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.requests.TurntableRequest;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.TurnRequest;


public abstract class TurningActorBase extends C2Component {

    private String id = "_gqBokeMxEDiawdooqVeh0w";

    public TurningActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleTurntableRequest(TurntableRequest turntableRequest);
    @RequestHandler
    public abstract void handleTurnRequest(TurnRequest turnRequest);




    protected void addSupportedMessagesTopDomain(){
    }

    protected void addSupportedMessagesBottomDomain(){
		supportedMessagesBottom.add(TurntableRequest.class);
		supportedMessagesBottom.add(TurntableStatusUpdateNotification.class);
		supportedMessagesBottom.add(TurnRequest.class);
    }

}