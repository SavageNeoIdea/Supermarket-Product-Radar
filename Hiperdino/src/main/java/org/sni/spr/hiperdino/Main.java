package org.sni.spr.hiperdino;

import org.sni.spr.hiperdino.controller.store.ActiveMQStore;
import org.sni.spr.hiperdino.controller.store.Store;
import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoFeeder;
import org.sni.spr.hiperdino.controller.feeder.scraper.HiperdinoPlaywrightManager;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoProductParser;
import org.sni.spr.hiperdino.controller.feeder.scraper.WebScraper;

public class Main {
    public static void main(String[] args) {
        try {
            HiperdinoProductParser productParser = new HiperdinoProductParser();
            WebScraper webScraper = new HiperdinoPlaywrightManager("35010");
            Store storer = new ActiveMQStore("tcp://localhost:61616", "admin", "admin");
            Controller controller = new Controller(
                    new HiperdinoFeeder(productParser, webScraper),
                    storer
            );
            controller.init();
        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }
}