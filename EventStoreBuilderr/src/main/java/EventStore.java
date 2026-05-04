import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventStore {

    private final String directoryPath = "events_data/";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public EventStore() {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            System.err.println("No se pudo crear el directorio de eventos: " + e.getMessage());
        }
    }

    public void saveEvent(String jsonContent) {
        String fileName = generateFileName();
        Path path = Paths.get(directoryPath + fileName);

        try {
            String entry = jsonContent + System.lineSeparator();
            Files.write(path, entry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("[\uD83D\uDCC4] Evento guardado en: " + fileName);
        } catch (IOException e) {
            System.err.println("Error al escribir en el EventStore: " + e.getMessage());
        }
    }

    private String generateFileName() {
        String datePart = LocalDate.now().format(formatter);
        return datePart + ".events";
    }
}