package configurations;

public class ServerInfo {

    private String name;
    private int portOffset;

    public ServerInfo(){}

    public ServerInfo(String name, int portOffset) {
        this.name = name;
        this.portOffset = portOffset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPortOffset() {
        return portOffset;
    }

    public void setPortOffset(int portOffset) {
        this.portOffset = portOffset;
    }
}
