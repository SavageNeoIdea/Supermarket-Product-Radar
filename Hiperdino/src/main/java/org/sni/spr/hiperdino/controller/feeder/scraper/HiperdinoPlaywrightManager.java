package org.sni.spr.hiperdino.controller.feeder.scraper;

import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoUrlParser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.sni.spr.hiperdino.controller.feeder.parser.ScraperRawPayload;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoPlaywrightManager implements WebScraper {
    private static final int LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 143;

    private final String postalCode;
    private final HumanBehaviorSimulator botBypass;

    public HiperdinoPlaywrightManager(String postalCode) {
        this.postalCode = postalCode;
        this.botBypass = new HumanBehaviorSimulator();
    }

    @Override
    public void startScraping(Consumer<ScraperRawPayload> rawDataConsumer) {
        try (PlaywrightSessionFactory sessionFactory = new PlaywrightSessionFactory()) {
            Page page = sessionFactory.createPage();
            initializeSession(page);
            List<String> targetUrls = extractLinks(page);
            for (String url : targetUrls) {
                scrapeCategory(page, url, rawDataConsumer);
            }
        }
    }

    private void initializeSession(Page page) {
        page.navigate("https://www.hiperdino.es/c9504/alimentacion/aceites.html");
        page.waitForTimeout(3000 + (Math.random() * 2000));
        page.click("button.amgdprcookie-button.-decline");
        if (postalCode == null || postalCode.length() != 5) {
            throw new IllegalArgumentException("Código postal inválido");
        }
        page.fill("input.input__text.required-entry.postal-input", postalCode);
        page.click("button[data-myaction='checkCp']");
    }

    private List<String> extractLinks(Page page) {
        List<String> urls = new ArrayList<>();
        List<Locator> categories = page.locator(".sidebar-item--wrapper a.link--wrapper").all();

        List<Locator> subList = categories.subList(LOWER_LIMIT, DUPLICATED_MENU_OFFSET - 2);
        for (Locator locator : subList) {
            urls.add(locator.getAttribute("href"));
        }
        return urls;
    }

    private void scrapeCategory(Page page, String url, Consumer<ScraperRawPayload> consumer) {
        String category = HiperdinoUrlParser.getCategory(url);
        String subcategory = HiperdinoUrlParser.getSubcategory(url);
        ProductResponseHandler handler = new ProductResponseHandler(page, subcategory, jsonBody -> {
            consumer.accept(new ScraperRawPayload(jsonBody, category, subcategory));
        });
        navigateToCategory(page, url);
        emitFirstPageGtm(page, category, subcategory, consumer);
        scrollUntilEnd(page);
    }

    private void scrollUntilEnd(Page page) {
        int failureAttempts = 0;
        while (failureAttempts < 6) {
            int previousCount = countProducts(page);
            botBypass.simulateHumanScroll(page);
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            failureAttempts = (countProducts(page) > previousCount) ? 0 : ++failureAttempts;
        }
    }

    private int countProducts(Page page) {
        return page.locator(".product-list-item.flex-item.loader-over").count();
    }

    private void navigateToCategory(Page page, String url) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Locator targetCategory = page.locator(".category-group")
                .filter(new Locator.FilterOptions().setHas(page.locator("a[href*='" + url + "']"))).first();

        if (!targetCategory.getAttribute("class").contains("dropdown-open")) {
            targetCategory.locator(".dropdown--trigger").click();
        }
        targetCategory.locator("a[href*='" + url + "']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void emitFirstPageGtm(Page page, String cat, String subCat, Consumer<ScraperRawPayload> consumer) {
        try {
            page.waitForFunction("() => typeof gtmProductDataObject !== 'undefined'");
            String gtmJson = (String) page.evaluate("() => JSON.stringify(gtmProductDataObject)");
            if (gtmJson != null && !gtmJson.trim().isEmpty()) {
                consumer.accept(new ScraperRawPayload(gtmJson, cat, subCat));
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo GTM inicial: " + e.getMessage());
        }
    }
}