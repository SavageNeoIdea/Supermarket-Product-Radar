package org.sni.spr.hiperdino;

import org.sni.spr.hiperdino.controller.store.HiperdinoSqlStore;
import org.sni.spr.hiperdino.controller.store.Store;
import org.sni.spr.hiperdino.controller.Controller;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoFeeder;
import org.sni.spr.hiperdino.controller.feeder.HiperdinoPlaywrightManager;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoProductParser;
import org.sni.spr.hiperdino.controller.feeder.WebScraper;

public class Main {

    public static void main(String[] args) {

        try {
            HiperdinoProductParser productParser = new HiperdinoProductParser();
            WebScraper webScraper = new HiperdinoPlaywrightManager("35010");
            Store storer = new HiperdinoSqlStore();
            Controller controller = new Controller(
                    new HiperdinoFeeder(productParser, webScraper),
                    storer
            );

            // controller.init();
            controller.startScheduler();

        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
