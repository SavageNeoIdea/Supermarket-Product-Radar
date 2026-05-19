package org.sni.spr.hiperdino.controller.feeder.scraper;
import org.sni.spr.hiperdino.controller.feeder.parser.ScraperRawPayload;

import java.util.function.Consumer;

public interface WebScraper {
    void startScraping(Consumer<ScraperRawPayload> rawDataConsumer);
}
