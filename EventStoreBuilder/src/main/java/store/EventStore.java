package store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventStore {

    private final String rootDirectory = "eventstore/";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern pattern = Pattern.compile("\"ss\"\\s*:\\s*\"(.*?)\"");

    public EventStore() {
        createDir(Paths.get(rootDirectory));
    }

    public void saveEvent(String topic, String jsonContent) {
        String sourceSite = extractSourceSite(jsonContent);
        Path targetPath = Paths.get(rootDirectory, topic, sourceSite);
        createDir(targetPath);
        String fileName = LocalDate.now().format(formatter) + ".events";
        Path finalFile = targetPath.resolve(fileName);
        try {
            String entry = jsonContent + System.lineSeparator();
            Files.write(finalFile, entry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error al persistir evento: " + e.getMessage());
        }
    }

    private String extractSourceSite(String json) {
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown_site";
    }

    private void createDir(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Error creando directorios: " + e.getMessage());
        }
    }
}