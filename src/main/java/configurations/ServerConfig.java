package configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

public class ServerConfig {

    public static String machineName;
    private final static String configsFolder = System.getProperty("user.dir")+ "/serverConfigs/";


    public static Optional<ServerInfo> loadServerConfigFromFileSystem() {
        String FILE = machineName + "ServerConfig.json";
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            String fileName = configsFolder + FILE;
            InputStream in = Files.newInputStream(Paths.get(fileName));//WiringUtils.class.getClassLoader().getResourceAsStream(fileName);
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

    public static void setMachineName(String machineName) {
        ServerConfig.machineName = machineName;
    }
}
