package components.turningactor.stateMachine;

import com.github.oxo42.stateless4j.StateMachine;

public class TurningStateMachine extends StateMachine<TurningStates, TurningTriggers> {

    public TurningStateMachine() {
        super(TurningStates.STOPPED, new TurningStateMachineConfig());
    }
}
