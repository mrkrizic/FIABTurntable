package components.conveyoractor.stateMachine;

import com.github.oxo42.stateless4j.StateMachineConfig;

import static components.conveyoractor.stateMachine.ConveyorStates.*;
import static components.conveyoractor.stateMachine.ConveyorTriggers.*;

public class ConveyorStateMachineConfig extends StateMachineConfig<ConveyorStates, ConveyorTriggers> {
    public ConveyorStateMachineConfig() {
        super();
        configure(STOPPING)
                .permit(DO_STOP, STOPPED);
        configure(STOPPED)
                .permit(DO_RESETTING, RESETTING);
        configure(RESETTING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_IDLE_LOADED, IDLE_LOADED)
                .permit(DO_IDLE_EMPTY, IDLE_EMPTY);
        configure(IDLE_EMPTY)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_LOADING, LOADING);
        configure(IDLE_LOADED)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_UNLOADING, UNLOADING);
        configure(LOADING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_IDLE_LOADED, IDLE_LOADED);
        configure(UNLOADING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_IDLE_EMPTY, IDLE_EMPTY);
    }
}
