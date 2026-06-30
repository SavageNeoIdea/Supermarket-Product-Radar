package org.sni.eventstorebuilder.store;

import org.sni.eventstorebuilder.model.Publisher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventStore {

    private static final Pattern pattern = Pattern.compile("\"ss\"\\s*:\\s*\"(.*?)\"");
    private final String rootDirectory = "eventstore/";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final List<Publisher> publishers;

    public EventStore(List<Publisher> publishers) {
        this.publishers = publishers;
    }

    public void prepareEventStorage() {
        createDirectory(Paths.get(rootDirectory));
        for (Publisher sub : publishers) {
            Path targetPath = Paths.get(rootDirectory, sub.getTopic(), sub.getSource());
            createDirectory(targetPath);
        }
    }

    private void createDirectory(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }

    public void registerProductEvent(String topic, String jsonContent) {
        String eventSource = extractEventSource(jsonContent);

        if (subscriberExistsFor(topic, eventSource)) {
            Path targetFile = buildEventFilePath(topic, eventSource);

            try {
                appendEventEntry(targetFile, jsonContent);
            } catch (IOException e) {
                System.err.println("Error while storing event: " + e.getMessage());
            }
        }
    }

    private String extractEventSource(String json) {
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown_site";
    }

    private boolean subscriberExistsFor(String topic, String source) {
        return publishers.stream()
                .anyMatch(sub -> sub.getTopic().equalsIgnoreCase(topic)
                        && sub.getSource().equalsIgnoreCase(source));
    }

    private void appendEventEntry(Path file, String content) throws IOException {
        String entry = content + System.lineSeparator();
        Files.write(file, entry.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private Path buildEventFilePath(String topic, String source) {
        Path path = Paths.get(rootDirectory, topic, source);
        String fileName = LocalDate.now().format(formatter) + ".events";
        return path.resolve(fileName);
    }
}