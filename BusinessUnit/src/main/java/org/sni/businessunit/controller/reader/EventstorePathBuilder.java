package org.sni.businessunit.controller.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class EventstorePathBuilder {

    public static Map<String, String> obtainLastDayEventsPathPerSource(String eventStorePath, String topic) {
        Path basePath = calculateBasePath(eventStorePath, topic);
        if (Files.isDirectory(basePath)) {
            return getPublishersLatestFiles(basePath);
        }
        System.err.println("ERROR: La ruta no existe o no es un directorio: " + basePath.toAbsolutePath());
        return Collections.emptyMap();
    }

    private static Path calculateBasePath(String eventStorePath, String topic) {
        return (topic == null) ? Paths.get(eventStorePath) : Paths.get(eventStorePath, topic);
    }

    private static Map<String, String> getPublishersLatestFiles(Path basePath) {
        Map<String, String> publisherFileMap = new HashMap<>();
        for (Path publisherDir : getPathSubdirectories(basePath)) {
            String publisherName = publisherDir.getFileName().toString();
            findLatestEventFile(publisherDir)
                    .map(path -> path.toAbsolutePath().toString())
                    .ifPresent(absolutePath -> publisherFileMap.put(publisherName, absolutePath));
        }
        return publisherFileMap;
    }

    private static List<Path> getPathSubdirectories(Path basePath) {
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