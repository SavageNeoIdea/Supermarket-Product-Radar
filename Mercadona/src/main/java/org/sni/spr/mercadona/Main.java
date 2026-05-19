package org.sni.spr.mercadona;
import org.sni.spr.mercadona.controller.*;
import org.sni.spr.mercadona.store.*;
import java.time.DateTimeException;
import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {
        try {
            ConfigReader configReader = new ConfigReader();
            String sitemapUrl = "https://tienda.mercadona.es/sitemap.xml";
            ProductProvider provider = new MercadonaProductProvider();
            HttpClientManager httpClient = new MercadonaHttpClient();
            ProductService productService = new MercadonaProductService(httpClient);
            Storer storer = new ActiveMQStorer();
            LocalTime executionTime = readAndValidateTime(configReader);
            Controller controller = new Controller(provider, productService, storer);
            controller.scheduleDailyRun(sitemapUrl, executionTime);
        } catch (DateTimeException e) {
            System.err.println("Error de configuración: La hora o minutos introducidos no son válidos (Horas: 0-23, Minutos: 0-59).");
        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static LocalTime readAndValidateTime(ConfigReader configReader) {
        var config = configReader.loadConfig("publishers", "mercadona");
        int hour = Integer.parseInt(config.get("ScheduleTimeHour"));
        int minutes = Integer.parseInt(config.get("ScheduleTimeMinutes"));
        return LocalTime.of(hour, minutes);
    }
}