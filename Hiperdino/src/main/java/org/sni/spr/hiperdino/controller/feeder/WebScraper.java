package org.sni.spr.hiperdino.controller.feeder;
import com.microsoft.playwright.Page;
import java.util.List;
import java.util.Map;

public interface WebScraper {
    public  List<Map<String, String>> extractProductRawData();
}
