package store.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class EventReader implements DataReader {

    private final String eventStorePath;
    private final String topic;

    public EventReader(String eventStorePath, String topic) {
        this.eventStorePath = eventStorePath;
        this.topic = topic;
    }

    @Override
    public Map<String, List<String>> readLastDay() {
        Map<String, String> lastEventFiles = PathBuilder.lastEventFiles(eventStorePath, topic);
        Map<String, List<String>> rawEvents = new HashMap<>();
        for (Map.Entry<String, String> entry : lastEventFiles.entrySet()) {
            String source = entry.getKey();
            String filePath = entry.getValue();
            try {
                List<String> validLines = readValidLines(filePath);
                if (!validLines.isEmpty()) {
                    rawEvents.put(source, validLines);
                    System.out.println("Fuente: " + source + " - Eventos cargados: " + validLines.size());
                }
            } catch (IOException e) {
                System.err.println("Error crítico leyendo eventos de la fuente: " + source + ". Se omitirá en esta pasada. Detalle: " + e.getMessage());
            }
        }

        return rawEvents;
    }

    private List<String> readValidLines(String filePath) throws IOException {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            return lines
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .toList();
        }
    }
}