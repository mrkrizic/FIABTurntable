package configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fiab.core.capabilities.wiring.WiringInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class WiringUtils {

    private static String wiringsFolder = "/wiringInfos/";
    private static String FILE = "Wiringinfo.json";

    public static Optional<HashMap<String, WiringInfo>> loadWiringInfoFromFileSystem(String machinePrefix) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //File file = new File(wiringsFolder+machinePrefix + FILE);
            //HashMap<String, WiringInfo> info = objectMapper.readValue(file, new TypeReference<HashMap<String, WiringInfo>>() {
            //});
            String fileName = wiringsFolder + machinePrefix + FILE;
            InputStream in = WiringUtils.class.getResourceAsStream(fileName);
            HashMap<String, WiringInfo> info = objectMapper.readValue(in,
                    new TypeReference<HashMap<String, WiringInfo>>() {
                    });
            for(String key : info.keySet()){
                WiringInfo i = info.get(key);
                System.out.println("Key: " + key + ", info: " + i.getRemoteCapabilityId() + i.getRemoteEndpointURL() + i.getRemoteNodeId()+ i.getRemoteRole());
            }
            return Optional.of(info);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static void writeWiringInfoToFileSystem(HashMap<String, WiringInfo> wireMap, String machinePrefix) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(
                    new FileOutputStream(machinePrefix + FILE), wireMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

