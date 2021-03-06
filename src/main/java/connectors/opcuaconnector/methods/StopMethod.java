package connectors.opcuaconnector.methods;


import akka.actor.ActorRef;
import fiab.core.capabilities.handshake.HandshakeCapability;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import msg.requests.OpcUaRequest;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class StopMethod extends AbstractMethodInvocationHandler {

    final Duration timeout = Duration.ofSeconds(2);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActorRef actor;

    public StopMethod(UaMethodNode methodNode, ActorRef actor) {
        super(methodNode);
        this.actor = actor;
    }

    @Override
    public Argument[] getInputArguments() {
        return new Argument[0];
    }

    @Override
    public Argument[] getOutputArguments() {
        return new Argument[0];
    }

    @Override
    protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) throws UaException {
        logger.debug("Invoking Stop() method of objectId={}", invocationContext.getObjectId());
        actor.tell(new OpcUaRequest(HandshakeCapability.ServerMessageTypes.Stop), ActorRef.noSender());
        return new Variant[0];
    }

}
