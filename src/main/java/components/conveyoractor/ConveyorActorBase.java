package components.conveyoractor;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.requests.ConveyorRequest;

public abstract class ConveyorActorBase extends C2Component {

    private String id = "_czbKceMxEDiawdooqVeh0w";

    public ConveyorActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleConveyorRequest(ConveyorRequest conveyorRequest);



}