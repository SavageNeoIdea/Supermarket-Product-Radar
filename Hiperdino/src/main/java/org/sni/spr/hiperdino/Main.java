import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoJsonProductParser;
import org.sni.spr.hiperdino.controller.store.ActivemqStore;
import org.sni.spr.hiperdino.controller.store.ConfigReader;
import org.sni.spr.hiperdino.controller.store.Store;
import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoFeeder;
import org.sni.spr.hiperdino.controller.feeder.scraper.HiperdinoPlaywrightManager;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoProductNameParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import java.time.DateTimeException;
import java.time.LocalTime;

public static void main(String[] args) {
    try {
        ConfigReader configReader = new ConfigReader();
        HiperdinoProductNameParser productNameParser = new HiperdinoProductNameParser();
        HiperdinoJsonProductParser productJsonParser = new HiperdinoJsonProductParser(productNameParser);
        WebScraper webScraper = new HiperdinoPlaywrightManager(configReader.
                loadConfig("publishers", "hiperdino").get("PostalCode"));
        Store storer = new ActivemqStore();
        Controller controller = new Controller(
                new HiperdinoFeeder(productJsonParser, webScraper),
                storer
        );
        LocalTime executionTime = readAndValidateTime(configReader);
        controller.startScheduler(executionTime);
    } catch (DateTimeException e) {
        System.err.println("Error de configuración: La hora o minutos introducidos no son válidos (Horas: 0-23, Minutos: 0-59).");
    } catch (Exception e) {
        System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
        e.printStackTrace();
    }
}

private static LocalTime readAndValidateTime(ConfigReader configReader) {
    var config = configReader.loadConfig("publishers", "hiperdino");
    int hour = Integer.parseInt(config.get("ScheduleTimeHour"));
    int minutes = Integer.parseInt(config.get("ScheduleTimeMinutes"));
    return LocalTime.of(hour, minutes);
}