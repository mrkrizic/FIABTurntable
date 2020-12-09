package components.turningactor.stateMachine;

import com.github.oxo42.stateless4j.StateMachineConfig;

import static components.turningactor.stateMachine.TurningStates.*;
import static components.turningactor.stateMachine.TurningTriggers.*;

public class TurningStateMachineConfig extends StateMachineConfig<TurningStates, TurningTriggers> {

    public TurningStateMachineConfig() {
        super();
        configure(STOPPING)
                .permit(DO_STOP, STOPPED);
        configure(STOPPED)
                .permit(DO_RESETTING, RESETTING);
        configure(RESETTING)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_IDLE, IDLE);
        configure(IDLE)
                .permit(DO_STOPPING, STOPPING)
                .permit(DO_STARTING, STARTING);
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
                .permit(DO_RESETTING, RESETTING);
    }
}
