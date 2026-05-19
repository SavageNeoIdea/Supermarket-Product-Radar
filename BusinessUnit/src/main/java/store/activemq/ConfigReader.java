package store.activemq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {

    public Map<String, String> loadConfig(String section, String key) {
        File configFile = findConfigFile();
        if (configFile == null) {
            System.err.println("ERROR: No se encontró config.json.");
            return null;
        }

        String content = readFile(configFile);
        if (content == null) return null;
        if (!content.contains("\"" + section + "\"")) {
            System.err.println("ERROR: No existe la sección " + section + " en config.json.");
            return null;
        }

        String sectionBlock = extractBlock(content, section);
        if (sectionBlock == null) return null;
        if (!sectionBlock.contains("\"" + key + "\"")) {
            System.err.println("ERROR: No existe la clave " + key + " dentro de " + section);
            return null;
        }

        String keyBlock = extractBlock(sectionBlock, key);
        if (keyBlock == null) return null;

        return parseKeyValueBlock(keyBlock);
    }

    private String extractBlock(String content, String key) {
        int keyIndex = content.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;
        int start = content.indexOf("{", keyIndex);
        if (start == -1) return null;
        int braceCount = 0;
        int end = start;
        for (; end < content.length(); end++) {
            char c = content.charAt(end);
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (braceCount == 0) break;
        }
        if (braceCount != 0) return null;
        return content.substring(start + 1, end).trim();
    }


    private Map<String, String> parseKeyValueBlock(String block) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = block.split(",");

        for (String pair : pairs) {
            if (pair.contains(":")) {
                String[] kv = pair.split(":", 2);
                String k = kv[0].replace("\"", "").trim();
                String v = kv[1].replace("\"", "").trim();
                map.put(k, v.equalsIgnoreCase("null") ? null : v);
            }
        }
        return map;
    }

    private File findConfigFile() {
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            configFile = new File("../config.json");
        }
        return configFile.exists() ? configFile : null;
    }

    private String readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            System.err.println("Error leyendo config.json: " + e.getMessage());
            return null;
        }
    }
}