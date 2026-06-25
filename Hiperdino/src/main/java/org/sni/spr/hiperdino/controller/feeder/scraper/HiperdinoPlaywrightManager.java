package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.Page;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoUrlParser;
import org.sni.spr.hiperdino.model.RawCategoryProductBatch;

import java.util.List;
import java.util.function.Consumer;

public class HiperdinoPlaywrightManager implements WebScraper {

    private final PlaywrightSessionInitializer sessionInitializer;

    public HiperdinoPlaywrightManager(String postalCode) {
        this.sessionInitializer = new PlaywrightSessionInitializer(postalCode);
    }

    @Override
    public void startScraping(Consumer<RawCategoryProductBatch> jsonConsumer) {
        try (Page activePage = sessionInitializer.setupHiperdinoPage()) {
            List<String> categoriesUrls = HiperdinoPlaywrightNavigator.extractCategoriesUrls(activePage);
            for (String url : categoriesUrls) {
                scrapeCategory(activePage, url, jsonConsumer);
            }
        }
    }

    private void scrapeCategory(Page page, String url, Consumer<RawCategoryProductBatch> jsonConsumer) {
        String category = HiperdinoUrlParser.getCategory(url);
        String subcategory = HiperdinoUrlParser.getSubcategory(url);
        ProductResponseHandler handler = new ProductResponseHandler(page, subcategory, jsonBody -> {
            jsonConsumer.accept(new RawCategoryProductBatch(jsonBody, category, subcategory));
        });
        try {
            HiperdinoPlaywrightNavigator.navigateToCategory(page, url);
            HiperdinoPlaywrightNavigator.emitFirstPageGtm(page, category, subcategory, jsonConsumer);
            HiperdinoPlaywrightNavigator.scrollUntilEnd(page);
        } finally {
            handler.removeNetworkInterceptor();
        }
    }
}