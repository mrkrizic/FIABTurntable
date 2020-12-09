package configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class ServerConfig {

    private final static String configsFolder = "/serverConfigs/";
    private final static String FILE = "FIABTurntableServerConfig.json";

    public static Optional<ServerInfo> loadServerConfigFromFileSystem() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String fileName = configsFolder + FILE;
            InputStream in = WiringUtils.class.getResourceAsStream(fileName);
            HashMap<String, ServerInfo> info = objectMapper.readValue(in,
                    new TypeReference<HashMap<String, ServerInfo>>() {
                    });
            if (info.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(info.entrySet().iterator().next().getValue());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
