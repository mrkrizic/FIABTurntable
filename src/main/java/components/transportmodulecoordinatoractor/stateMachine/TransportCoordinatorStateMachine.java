package components.transportmodulecoordinatoractor.stateMachine;

import com.github.oxo42.stateless4j.StateMachine;

public class TransportCoordinatorStateMachine
        extends StateMachine<TransportCoordinatorStates, TransportCoordinatorTriggers> {

    public TransportCoordinatorStateMachine() {
        super(TransportCoordinatorStates.STOPPED, new TransportCoordinatorStateMachineConfig());  //We start in stopped
    }
}
