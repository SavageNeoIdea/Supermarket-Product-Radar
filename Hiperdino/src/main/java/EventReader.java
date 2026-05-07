import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EventReader implements DataReader {

    private final String eventStorePath;
    private final String topic;

    public EventReader(String eventStorePath) {
        this(eventStorePath, null);
    }

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

                List<String> lines = Files.readAllLines(Path.of(filePath));
                rawEvents.put(source, lines);

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