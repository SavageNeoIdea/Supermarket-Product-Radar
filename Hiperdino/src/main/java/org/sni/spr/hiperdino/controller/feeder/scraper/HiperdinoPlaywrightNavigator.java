package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.sni.spr.hiperdino.model.RawCategoryProductBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HiperdinoPlaywrightNavigator {

    private static final int LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 143;

    private HiperdinoPlaywrightNavigator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<String> extractCategoriesUrls(Page page) {
        List<String> urls = new ArrayList<>();
        List<Locator> categories = page.locator(".sidebar-item--wrapper a.link--wrapper").all();
        List<Locator> subList = categories.subList(LOWER_LIMIT, DUPLICATED_MENU_OFFSET - 2);
        for (Locator locator : subList) {
            urls.add(locator.getAttribute("href"));
        }
        return urls;
    }

    public static void navigateToCategory(Page page, String url) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Locator targetCategory = page.locator(".category-group")
                .filter(new Locator.FilterOptions().setHas(page.locator("a[href*='" + url + "']"))).first();

        if (!targetCategory.getAttribute("class").contains("dropdown-open")) {
            targetCategory.locator(".dropdown--trigger").click();
        }
        targetCategory.locator("a[href*='" + url + "']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public static void scrollUntilEnd(Page page) {
        int failureAttempts = 0;
        while (failureAttempts < 6) {
            int previousCount = countProducts(page);
            HumanBehaviorSimulator.simulateHumanScroll(page);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            failureAttempts = (countProducts(page) > previousCount) ? 0 : ++failureAttempts;
        }
    }

    public static void emitFirstPageGtm(Page page, String cat, String subCat, Consumer<RawCategoryProductBatch> consumer) {
        try {
            page.waitForFunction("() => typeof gtmProductDataObject !== 'undefined'");
            String gtmJson = (String) page.evaluate("() => JSON.stringify(gtmProductDataObject)");
            if (gtmJson != null && !gtmJson.trim().isEmpty()) {
                consumer.accept(new RawCategoryProductBatch(gtmJson, cat, subCat));
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo GTM inicial: " + e.getMessage());
        }
    }

    private static int countProducts(Page page) {
        return page.locator(".product-list-item.flex-item.loader-over").count();
    }
}