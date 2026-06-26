package org.sni.businessunit.controller.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EventReader implements DataReader {

    private final Path eventStorePath;
    private final String topic;

    public EventReader(String eventStorePath, String topic) {
        this.eventStorePath = Path.of(eventStorePath);
        this.topic = topic;
    }

    @Override
    public Map<String, List<String>> readLastDay() {
        Map<String, String> lastEventFiles = PathBuilder.lastEventFiles(eventStorePath.toString(), topic);
        Map<String, List<String>> rawEvents = new HashMap<>();
        for (Map.Entry<String, String> entry : lastEventFiles.entrySet()) {
            String source = entry.getKey();
            Path filePath = Path.of(entry.getValue());
            try {
                List<String> validLines = readValidLines(filePath);
                if (!validLines.isEmpty()) {
                    rawEvents.put(source, validLines);
                }
            } catch (IOException e) {
                System.err.printf("Error leyendo fuente [%s] en ruta [%s]: %s%n",
                        source, filePath, e.getMessage());
            }
        }
        return rawEvents;
    }

    private List<String> readValidLines(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines
                    .filter(line -> line != null && !line.isBlank())
                    .toList();
        }
    }
}