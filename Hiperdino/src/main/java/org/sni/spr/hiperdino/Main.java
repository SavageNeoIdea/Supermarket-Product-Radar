import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoProductFeeder;
import org.sni.spr.hiperdino.controller.feeder.ProductFeeder;
import org.sni.spr.hiperdino.controller.feeder.scraper.HiperdinoPlaywrightManager;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;
import org.sni.spr.hiperdino.store.ActivemqStore;
import org.sni.spr.hiperdino.store.ConfigReader;
import org.sni.spr.hiperdino.store.Store;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Map;

public static void main(String[] args) {
    try {
        ConfigReader configReader = new ConfigReader();
        String postalCode = configReader.loadConfig("publishers", "hiperdino").get("postalCode");
        WebScraper webScraper = new HiperdinoPlaywrightManager(postalCode);
        Store storer = new ActivemqStore();
        ProductFeeder feeder = new HiperdinoProductFeeder();
        Controller controller = new Controller(feeder, storer, webScraper);
        LocalTime executionTime = readAndValidateTime(configReader);
        controller.init();

    } catch (DateTimeException e) {
        System.err.println("Error de configuración: La hora o minutos introducidos no son válidos (Horas: 0-23, Minutos: 0-59).");
    } catch (Exception e) {
        System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
        e.printStackTrace();
    }
}

private static LocalTime readAndValidateTime(ConfigReader configReader) {
    Map<String, String> config = configReader.loadConfig("publishers", "hiperdino");
    int hour = Integer.parseInt(config.get("ScheduleTimeHour"));
    int minutes = Integer.parseInt(config.get("ScheduleTimeMinutes"));
    return LocalTime.of(hour, minutes);
}