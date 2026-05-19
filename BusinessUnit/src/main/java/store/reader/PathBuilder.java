package store.reader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class PathBuilder {

    public static Map<String, String> lastEventFiles(String eventStorePath, String topic) {
        Path basePath = (topic == null) ? Paths.get(eventStorePath) : Paths.get(eventStorePath, topic);
        if (!Files.isDirectory(basePath)) {
            System.err.println("ERROR: La ruta no existe o no es un directorio: " + basePath.toAbsolutePath());
            return Collections.emptyMap();
        }
        return resolveLatestFiles(basePath);
    }

    private static Map<String, String> resolveLatestFiles(Path basePath) {
        Map<String, String> providerFileMap = new HashMap<>();
        for (Path providerDir : listSubdirectories(basePath)) {
            String providerName = providerDir.getFileName().toString();
            findLatestEventFile(providerDir)
                    .map(path -> path.toAbsolutePath().toString())
                    .ifPresent(absolutePath -> providerFileMap.put(providerName, absolutePath));
        }
        return providerFileMap;
    }

    private static List<Path> listSubdirectories(Path basePath) {
        try (Stream<Path> stream = Files.list(basePath)) {
            return stream.filter(Files::isDirectory).toList();
        } catch (IOException e) {
            System.err.println("Error listando directorios de proveedores en " + basePath + ": " + e.getMessage());
            return List.of();
        }
    }

    private static Optional<Path> findLatestEventFile(Path providerDir) {
        try (Stream<Path> fileStream = Files.list(providerDir)) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".events"))
                    .max(Comparator.comparing(Path::getFileName));
        } catch (IOException e) {
            System.err.println("Error al buscar archivo .events en " + providerDir + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}