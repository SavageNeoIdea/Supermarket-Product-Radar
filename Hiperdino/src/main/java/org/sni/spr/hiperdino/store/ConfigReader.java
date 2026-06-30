package org.sni.spr.hiperdino.store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {

    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CONFIG_FILE_FALLBACK = "../config.json";

    public Map<String, String> loadConfig(String section, String key) {
        File configFile = findConfigFile();
        String content = readFile(configFile);
        String sectionBlock = extractBlock(content, section);
        String keyBlock = extractBlock(sectionBlock, key);
        return parseKeyValuePairs(keyBlock);
    }

    private File findConfigFile() {
        File file = new File(CONFIG_FILE_NAME);
        if (file.exists()) return file;
        file = new File(CONFIG_FILE_FALLBACK);
        if (file.exists()) return file;
        throw new IllegalStateException("No se encontró " + CONFIG_FILE_NAME + " en ninguna ruta conocida");
    }

    private String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + file.getName() + ": " + e.getMessage(), e);
        }
        return content.toString();
    }

    private String extractBlock(String content, String key) {
        int keyIndex = content.indexOf("\"" + key + "\"");
        if (keyIndex == -1) throw new IllegalArgumentException("Clave no encontrada en config: " + key);

        int blockStart = content.indexOf("{", keyIndex);
        if (blockStart == -1) throw new IllegalArgumentException("No hay bloque { } para la clave: " + key);

        int blockEnd = findClosingBrace(content, blockStart);
        return content.substring(blockStart + 1, blockEnd).trim();
    }

    private int findClosingBrace(String content, int openBraceIndex) {
        int depth = 0;
        for (int i = openBraceIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (depth == 0) return i;
        }
        throw new IllegalArgumentException("Bloque JSON sin cerrar a partir del índice " + openBraceIndex);
    }

    private Map<String, String> parseKeyValuePairs(String block) {
        Map<String, String> result = new HashMap<>();
        for (String pair : block.split(",")) {
            if (!pair.contains(":")) continue;
            String[] kv = pair.split(":", 2);
            String key = kv[0].replace("\"", "").trim();
            String value = kv[1].replace("\"", "").trim();
            result.put(key, value.equalsIgnoreCase("null") ? null : value);
        }
        return result;
    }
}