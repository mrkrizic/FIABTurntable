package configurations;

import akka.japi.pf.FI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.jndi.toolkit.url.Uri;
import fiab.core.capabilities.wiring.WiringInfo;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WiringUtils {

    private static String wiringsFolder = "/wiringInfos/";
    private static String FILE = "Wiringinfo.json";

    public static Optional<HashMap<String, WiringInfo>> loadWiringInfoFromFileSystem(String machinePrefix) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //File file = new File(wiringsFolder+machinePrefix + FILE);
            //HashMap<String, WiringInfo> info = objectMapper.readValue(file, new TypeReference<HashMap<String, WiringInfo>>() {
            //});
            String fileName = wiringsFolder+machinePrefix + FILE;
            InputStream in = WiringUtils.class.getResourceAsStream(fileName);
            HashMap<String, WiringInfo> info = objectMapper.readValue(in,
                    new TypeReference<HashMap<String, WiringInfo>>() {});
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

