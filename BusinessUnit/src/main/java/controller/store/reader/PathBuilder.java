package controller.store.reader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class PathBuilder {

    public static Map<String, String> lastEventFiles(String eventStorePath, String topic) {
        Path basePath = (topic == null) ? Paths.get(eventStorePath) : Paths.get(eventStorePath, topic);
        System.out.println("DEBUG - Buscando en ruta absoluta: " + basePath.toAbsolutePath());

        if (!isValidDirectory(basePath)) {
            System.err.println("ERROR: La ruta no existe o no es un directorio.");
            return Collections.emptyMap();
        }

        return findLatestEventFilesPerProvider(basePath);
    }

    private static Map<String, String> findLatestEventFilesPerProvider(Path basePath) {
        Map<String, String> result = new HashMap<>();

        try (Stream<Path> providerDirectories = Files.list(basePath)) {
            List<Path> providers = providerDirectories.filter(Files::isDirectory).toList();

            for (Path providerDir : providers) {
                findLatestEventFile(providerDir).ifPresent(latestPath -> {
                    String providerName = providerDir.getFileName().toString();
                    result.put(providerName, latestPath.toAbsolutePath().toString());
                });
            }
        } catch (IOException e) {
            System.err.println("Error accediendo al sistema de archivos: " + e.getMessage());
        }
        return result;
    }

    private static boolean isValidDirectory(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    private static Optional<Path> findLatestEventFile(Path providerDir) {
        try (Stream<Path> fileStream = Files.list(providerDir)) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".events"))
                    .max(Comparator.comparing(path -> extractDate(path.getFileName().toString())));
        } catch (IOException e) {
            System.err.println("Error al listar archivos en " + providerDir + ": " + e.getMessage());
            return Optional.empty();
        }
    }


    private static String extractDate(String fileName) {

        int dotIndex = fileName.indexOf('.');

        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }

        return fileName;
    }
}