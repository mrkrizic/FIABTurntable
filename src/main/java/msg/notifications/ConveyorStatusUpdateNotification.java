package msg.notifications;

import c2akka.c2messages.C2Notification;
import components.conveyoractor.stateMachine.ConveyorStates;
import fiab.core.capabilities.BasicMachineStates;

public class ConveyorStatusUpdateNotification extends C2Notification {

    private final ConveyorStates conveyorState;

    public ConveyorStatusUpdateNotification(ConveyorStates conveyorState) {

        this.conveyorState = conveyorState;
    }

    public ConveyorStates getConveyorState() {
        return conveyorState;
    }
}