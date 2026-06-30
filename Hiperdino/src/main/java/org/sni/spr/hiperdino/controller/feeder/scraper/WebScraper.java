package org.sni.spr.hiperdino.controller.feeder.scraper;

import org.sni.spr.hiperdino.model.RawCategoryProductBatch;

import java.util.function.Consumer;

public interface WebScraper {
    void startScraping(Consumer<RawCategoryProductBatch> jsonConsumer);
}
