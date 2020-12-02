import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import fiab.core.capabilities.transport.TurntableModuleWellknownCapabilityIdentifiers;
import fiab.opcua.client.OPCUAClientFactory;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.nodes.MethodNode;
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.client.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class IntegrationTest {

    private ActorSystem system;
    private OpcUaClient client;
    private String endpointURL;

    @Before
    public void setup() {
        system = ActorSystem.create();
        endpointURL = "opc.tcp://127.0.0.1:4840/milo";
        //endpointURL = "opc.tcp://192.168.178.20:4840/milo";
        this.client = null;
        try {
            this.client = new OPCUAClientFactory().createClient(endpointURL);
            this.client.connect().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void teardown() {
        system.terminate();
    }

    @Test
    public void testDiscoveryAndResetSuccessful() throws ExecutionException, InterruptedException {
        //NodeId statusNode = browseServerNodesRecursively(client.getAddressSpace()
        //        .browse(Identifiers.RootFolder).get(), "Turntable_FU/STATE");
        //System.out.println(statusNode);
        //DataValue statusValue = client.readValue(1000, TimestampsToReturn.Both, statusNode).get();
        //System.out.println("State=" + statusValue.getValue().toString());
                    /*NodeId stopNode = browseServerNodesRecursively(client.getAddressSpace()
                            .browse(Identifiers.RootFolder).get(), "Stop");
                    System.out.println(callTurntableFuMethod(stopNode, new Variant[]{}).get());*/
                    /*NodeId resetNode = browseServerNodesRecursively(client.getAddressSpace()
                            .browse(Identifiers.RootFolder).get(), "Reset");
                    callMethod(resetNode).get();*/
        //NodeId transportNode = browseServerNodesRecursively(client.getAddressSpace()
        //        .browse(Identifiers.RootFolder).get(), "TransportRequest");
        NodeId transportNode = new NodeId(2, "FIABTurntable/Turntable_FU/TransportRequest");
        Variant[] inputArgs = new Variant[]{
                new Variant("NORTH_CLIENT"),
                new Variant("SOUTH_CLIENT"),
                new Variant("Order1"),
                new Variant("TReq1")};
        callMethod(transportNode, inputArgs).get();
    }

    private NodeId browseServerNodesRecursively(List<Node> nodes, String name) throws ExecutionException, InterruptedException {
        NodeId nodeId = null;
        for (Node node : nodes) {
            if (nodeId != null) {
                return nodeId;
            }
            browseServerNodesRecursively(client.getAddressSpace().browse(node.getNodeId().get()).get(), name);
            if (Objects.requireNonNull(node.getBrowseName().get().getName()).contains(name)) {
                if (node instanceof UaMethodNode) {
                    System.out.println("Found " + name + " method node: " + node.getNodeId().get());
                    nodeId = node.getNodeId().get();
                }
            }
        }
        return nodeId;
    }

    protected CompletableFuture<Boolean> callMethod(NodeId methodId) {

        CallMethodRequest request = new CallMethodRequest(
                new NodeId(2, "FIABTurntable/Turntable_FU"), methodId, new Variant[]{});

        return client.call(request).thenCompose(result -> {
            StatusCode statusCode = result.getStatusCode();

            if (statusCode.isGood()) {
                return CompletableFuture.completedFuture(Boolean.TRUE);
            } else {
                StatusCode[] inputArgumentResults = result.getInputArgumentResults();
                for (int i = 0; i < inputArgumentResults.length; i++) {
                    System.err.println("inputArgumentResults=" + i + ", result=" + inputArgumentResults[i]);
                }
                CompletableFuture<Boolean> f = new CompletableFuture<>();
                f.completeExceptionally(new UaException(statusCode));
                return f;
            }
        });
    }

    protected CompletableFuture<String> callMethod(NodeId methodId, Variant[] inputArgs) {
        CallMethodRequest request = new CallMethodRequest(
                new NodeId(2, "FIABTurntable/Turntable_FU"), methodId, inputArgs);
        System.out.println(Arrays.stream(request.getInputArguments()).collect(Collectors.toList()));
        return client.call(request).thenCompose(result -> {
            StatusCode statusCode = result.getStatusCode();
            if (statusCode.isGood()) {
                String value = (String) (result.getOutputArguments())[0].getValue();
                return CompletableFuture.completedFuture(value);
            } else {
                StatusCode[] inputArgumentResults = result.getInputArgumentResults();
                for (int i = 0; i < inputArgumentResults.length; i++) {
                    System.err.println("inputArgumentResults" + i + "=" + inputArgumentResults[i]);
                }
                CompletableFuture<String> f = new CompletableFuture<>();
                f.completeExceptionally(new UaException(statusCode));
                return f;
            }
        });
    }
}