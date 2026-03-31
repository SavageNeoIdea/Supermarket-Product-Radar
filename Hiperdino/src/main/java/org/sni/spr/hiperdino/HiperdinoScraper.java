package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HiperdinoScraper {

    private Map<String, String> categoriesMap = new LinkedHashMap<>();

    private static final String URL_BASE = "https://www.hiperdino.es/c9504/alimentacion/aceites.html";
    private static final int SPECIAL_OFFERS_TO_EXCLUDE_LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 142;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final String postalCode;

    public HiperdinoScraper(String postalCode) {
        if (postalCode == null || postalCode.length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 números");
        }

        this.postalCode = postalCode;
    }

    public Page getPage() {
        return this.page;
    }

    public Map<String, String> getCategoriesMap() {
        return categoriesMap;
    }

    public void close() {
        this.playwright.close();
    }

    public void init(){
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        this.context = browser.newContext(new Browser.NewContextOptions().setLocale("es-ES"));
        this.page = this.context.newPage();
        this.page.navigate(URL_BASE);
}

    public void manageCookies(){
        String cookieDeclineButton = "button.amgdprcookie-button.-decline";
        this.page.click(cookieDeclineButton);
    }

    public boolean writePostalCode() {
        String textBlock = "input.input__text.required-entry.postal-input";
        String enterButton = "button[data-myaction='checkCp']";

        this.page.fill(textBlock, this.postalCode);
        this.page.click(enterButton);

        return !this.page.getByText("Introduce otro código postal").isVisible();
    }

    public Map<String, String> extractCategories() {
        List<Locator> categoryUrlLocation = page.locator(".sidebar-item--wrapper a.link--wrapper").all();

        Pattern pattern = Pattern.compile("https://www\\.hiperdino\\.es/c\\d+/([^/]+)/.*\\.html");

        for (int categoriyIndex = SPECIAL_OFFERS_TO_EXCLUDE_LOWER_LIMIT;
             categoriyIndex < categoryUrlLocation.size() - DUPLICATED_MENU_OFFSET;
             categoriyIndex++) {

            String url = categoryUrlLocation.get(categoriyIndex).getAttribute("href");

            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {

                String category = matcher.group(1).replace("-", " ");

                String innerText = categoryUrlLocation.get(categoriyIndex).innerText().trim();
                String subcategory = innerText.replace("-", " ");

                String name = category + " - " + subcategory;

                getCategoriesMap().put(name, url);
            }
        }

        categoriesMap.put("marca propia - Marca propia",
                "https://www.hiperdino.es/c9504/marca-propia/marca-propia.html");

        return categoriesMap;
    }

    private void crawler(String category){}

}