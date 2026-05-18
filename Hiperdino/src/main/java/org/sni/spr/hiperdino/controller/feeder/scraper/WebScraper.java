package org.sni.spr.hiperdino.controller.feeder.scraper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface WebScraper {
    void startScraping(Consumer<List<String>> rawDataConsumer);
}
