package org.sni.spr.hiperdino;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoJsonProductParser;
import org.sni.spr.hiperdino.controller.store.ActivemqStore;
import org.sni.spr.hiperdino.controller.store.ConfigReader;
import org.sni.spr.hiperdino.controller.store.Store;
import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoFeeder;
import org.sni.spr.hiperdino.controller.feeder.scraper.HiperdinoPlaywrightManager;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoProductNameParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;

public class Main {
    private static final String POSTAL_CODE = new ConfigReader().
            loadConfig("publishers", "hiperdino").
            get("postalCode");
    public static void main(String[] args) {
        try {
            HiperdinoProductNameParser productNameParser = new HiperdinoProductNameParser();
            HiperdinoJsonProductParser productJsonParser = new HiperdinoJsonProductParser(productNameParser);
            WebScraper webScraper = new HiperdinoPlaywrightManager(POSTAL_CODE);
            Store storer = new ActivemqStore();
            Controller controller = new Controller(
                    new HiperdinoFeeder(productJsonParser, webScraper),
                    storer
            );
            controller.init();
        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }
}