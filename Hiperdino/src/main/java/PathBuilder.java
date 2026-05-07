import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathBuilder {

    public static Map<String, String> lastEventFiles(
            String eventStorePath,
            String topic
    ) {

        Path basePath = topic == null
                ? Paths.get(eventStorePath)
                : Paths.get(eventStorePath, topic);

        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();

        try (Stream<Path> providerDirectories = Files.list(basePath)) {

            List<Path> providers = providerDirectories
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            for (Path providerDir : providers) {

                Optional<Path> latestFile = Files.list(providerDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".events"))
                        .max(Comparator.comparing(
                                path -> extractDate(path.getFileName().toString())
                        ));

                latestFile.ifPresent(path -> {
                    String providerName = providerDir.getFileName().toString();

                    result.put(providerName, path.toString());
                });
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static String extractDate(String fileName) {

        int dotIndex = fileName.indexOf('.');

        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }

        return fileName;
    }
}