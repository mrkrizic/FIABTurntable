package components.transportmodulecoordinatoractor.handshake;

import fiab.handshake.actor.LocalEndpointStatus;
import msg.requests.TurnRequest;

public class EndpointToDirectionMapper {

    public static TurnRequest.Direction mapEndpointToDirection(LocalEndpointStatus endpointStatus) {
        String capabilityId = endpointStatus.getCapabilityId();
        if (capabilityId.startsWith("NORTH")) {
            return TurnRequest.Direction.NORTH;
        } else if (capabilityId.startsWith("EAST")) {
            return TurnRequest.Direction.EAST;
        } else if (capabilityId.startsWith("SOUTH")) {
            return TurnRequest.Direction.SOUTH;
        } else if (capabilityId.startsWith("WEST")) {
            return TurnRequest.Direction.WEST;
        }
        return null;
    }
}
