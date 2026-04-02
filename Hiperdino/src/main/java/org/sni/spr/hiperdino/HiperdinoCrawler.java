package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoCrawler{

    private final String postalCode;
    private final BrowserManager browserManager;

    private final Map<String, List<String>> categoriesMap;

    public HiperdinoCrawler(String postalCode, BrowserManager browserManager) {
        if (postalCode == null || postalCode.length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 números");
        }
        this.postalCode = postalCode;
        this.browserManager = browserManager;
        this.categoriesMap = new LinkedHashMap<>();
    }

    public void prepareSession(){
        manageCookies();
        writePostalcode();
    }

    public void extractCategoriesUrls() {
        browserManager.initLocatorList(".sidebar-item--wrapper a.link--wrapper");
        Pattern pattern = UrlParser.initPattern();

        for (Locator currentLocation : browserManager.getCategoriesUrls()) {
            String url = currentLocation.getAttribute("href");
            Matcher matcher = pattern.matcher(url);
            registerCategoryDetails(matcher, url);
        }

        categoriesMap.put("marca propia - Marca propia", List.of("https://www.hiperdino.es/c9504" +
                "/marca-propia/marca-propia.html", "marca propia", "Marca propia"));
    }

    private boolean manageCookies() {
        String cookieDeclineButton = "button.amgdprcookie-button.-decline";
        browserManager.click(cookieDeclineButton);
        return true;
    }

    private boolean writePostalcode() {
        String textBlock = "input.input__text.required-entry.postal-input";
        String Button = "button[data-myaction='checkCp']";
        browserManager.fill(textBlock, this.postalCode);
        browserManager.click(Button);
        return true;
    }

    private void registerCategoryDetails(Matcher matcher, String url) {
            List<String> formatedData = UrlParser.getFormatedData(matcher, url);
            String name = formatedData.getFirst();
            List<String> categoryDetails = formatedData.subList(1, formatedData.size());
            categoriesMap.put(name, categoryDetails);
        }

    public Map<String, List<String>> getCategoriesMap() {return categoriesMap;}
}
