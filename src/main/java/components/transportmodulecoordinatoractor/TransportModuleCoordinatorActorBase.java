package components.transportmodulecoordinatoractor;

import akka.actor.ActorRef;
import c2akka.c2bricks.c2component.C2Component;
import c2akka.c2bricks.c2component.NotificationHandler;
import c2akka.c2bricks.c2component.RequestHandler;
import c2akka.c2messages.C2Notification;
import msg.notifications.ConveyorStatusUpdateNotification;
import msg.notifications.MachineStatusUpdateNotification;
import msg.notifications.TurntableStatusUpdateNotification;
import msg.requests.InternalTransportModuleRequest;
import msg.requests.OpcUaRequest;

public abstract class TransportModuleCoordinatorActorBase extends C2Component {

    private String id = "_r7zUQOMwEDiawdooqVeh0w";

    public TransportModuleCoordinatorActorBase(ActorRef connectorTopDomain, ActorRef connectorBottomDomain) {
        super(connectorTopDomain, connectorBottomDomain);
    }

    @RequestHandler
    public abstract void handleOpcUaRequest(OpcUaRequest opcUaRequest);

    @NotificationHandler
    public abstract void handleMachineStatusUpdateNotification(MachineStatusUpdateNotification machineStatusUpdateNotification);

    @RequestHandler
    public abstract void handleInternalTransportModuleRequest(InternalTransportModuleRequest request);

    @NotificationHandler
    public abstract void handleConveyorStatusUpdateNotification(ConveyorStatusUpdateNotification conveyorStatusUpdateNotification);

    @NotificationHandler
    public abstract void handleTurntableStatusUpdateNotification(TurntableStatusUpdateNotification turntableStatusUpdateNotification);
}