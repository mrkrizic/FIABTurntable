package components.conveyoractor.stateMachine;

import com.github.oxo42.stateless4j.StateMachine;

public class ConveyorStateMachine extends StateMachine<ConveyorStates, ConveyorTriggers> {
    public ConveyorStateMachine() {
        super(ConveyorStates.STOPPED, new ConveyorStateMachineConfig());
    }
}
