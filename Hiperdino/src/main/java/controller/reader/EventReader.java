package controller.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EventReader implements DataReader {

    private final String eventStorePath;
    private final String topic;

    public EventReader(String eventStorePath, String topic) {
        this.eventStorePath = eventStorePath;
        this.topic = topic;
    }

    @Override
    public Map<String, List<String>> readLastDay() {

        Map<String, String> lastEventFiles =
                PathBuilder.lastEventFiles(eventStorePath, topic);

        Map<String, List<String>> rawEvents = new HashMap<>();

        for (Map.Entry<String, String> entry : lastEventFiles.entrySet()) {

            String source = entry.getKey();
            String filePath = entry.getValue();

            try {
                List<String> allLines = Files.readAllLines(Path.of(filePath));
                List<String> validLines = allLines.stream()
                        .filter(line -> line != null && !line.trim().isEmpty())
                        .collect(Collectors.toList());

                if (!validLines.isEmpty()) {
                    rawEvents.put(source, validLines);
                }

                System.out.println("Fuente: " + source + " - Eventos cargados: " + validLines.size());

            } catch (IOException e) {
                throw new RuntimeException(
                        "Error leyendo eventos de " + source,
                        e
                );
            }
        }

        return rawEvents;
    }
}