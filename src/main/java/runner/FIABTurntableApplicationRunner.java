package runner;

import java.util.Optional;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import c2akka.c2architecture.C2StartMessage;
import c2akka.c2architecture.ApplicationRunner;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FIABTurntableApplicationRunner implements ApplicationRunner {

    private final ActorSystem actorSystem;
    private final List<ActorRef> bricks;

    @Inject
    public FIABTurntableApplicationRunner(ActorSystem actorSystem , @Named("OpcUaConnector") ActorRef opcUaConnector, @Named("IntraMachineConnector") ActorRef intraMachineConnector, @Named("HandshakeConnector") ActorRef handshakeConnector, @Named("TransportModuleCoordinatorActor") ActorRef transportModuleCoordinatorActor, @Named("ConveyorActor") ActorRef conveyorActor, @Named("TurningActor") ActorRef turningActor, @Named("ClientHandshakeNorthActor") ActorRef clientHandshakeNorthActor, @Named("ServerHandshakeEastActor") ActorRef serverHandshakeEastActor, @Named("ClientHandshakeEastActor") ActorRef clientHandshakeEastActor, @Named("ClientHandshakeSouthActor") ActorRef clientHandshakeSouthActor, @Named("ClientHandshakeWestActor") ActorRef clientHandshakeWestActor) {
        this.actorSystem = actorSystem;
        this.bricks = new CopyOnWriteArrayList<>();
        bricks.add(opcUaConnector);
        bricks.add(intraMachineConnector);
        bricks.add(handshakeConnector);
        bricks.add(transportModuleCoordinatorActor);
        bricks.add(conveyorActor);
        bricks.add(turningActor);
        bricks.add(clientHandshakeNorthActor);
        bricks.add(serverHandshakeEastActor);
        bricks.add(clientHandshakeEastActor);
        bricks.add(clientHandshakeSouthActor);
        bricks.add(clientHandshakeWestActor);

    }

    @Override
    public void start() {
        for(ActorRef ref : bricks){
            ref.tell(new C2StartMessage(), ActorRef.noSender());
        }
    }
}