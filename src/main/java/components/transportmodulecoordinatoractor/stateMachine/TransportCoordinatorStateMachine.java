package components.transportmodulecoordinatoractor.stateMachine;

import com.github.oxo42.stateless4j.StateMachine;
import fiab.core.capabilities.BasicMachineStates;

public class TransportCoordinatorStateMachine
        extends StateMachine<BasicMachineStates, TransportCoordinatorTriggers> {

    public TransportCoordinatorStateMachine() {
        super(BasicMachineStates.STOPPED, new TransportCoordinatorStateMachineConfig());  //We start in stopped
    }
}
