package org.sni.spr.hiperdino.controller.feeder.scraper;
import java.util.List;
import java.util.Map;

public interface WebScraper {
    public  List<Map<String, String>> extractProductRawData();
}
