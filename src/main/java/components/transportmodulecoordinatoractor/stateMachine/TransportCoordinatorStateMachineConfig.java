package components.transportmodulecoordinatoractor.stateMachine;

import com.github.oxo42.stateless4j.StateMachineConfig;

import static components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorStates.*;
import static components.transportmodulecoordinatoractor.stateMachine.TransportCoordinatorTriggers.*;

public class TransportCoordinatorStateMachineConfig
        extends StateMachineConfig<TransportCoordinatorStates, TransportCoordinatorTriggers> {

    TransportCoordinatorStateMachineConfig() {
        configure(STOPPING)
                .permit(DO_STOP, STOPPED);
        configure(STOPPED)
                .permit(DO_RESET, RESETTING);
        configure(RESETTING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_IDLE, IDLE);
        configure(IDLE)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_START, STARTING);
        configure(STARTING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_EXECUTE, EXECUTE);
        configure(EXECUTE)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_COMPLETING, COMPLETING);
        configure(COMPLETING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_COMPLETE, COMPLETE);
        configure(COMPLETE)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_RESET, RESETTING);
    }
}
