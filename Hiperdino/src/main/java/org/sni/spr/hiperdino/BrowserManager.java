package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;

import java.util.List;

public class BrowserManager {
    private static final int LOWER_LIMIT = 12;
    private static final int DUPLICATED_MENU_OFFSET = 139;
    private static final String URL_BASE = "https://www.hiperdino.es/c9504/alimentacion/aceites.html";

    private final boolean headless;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private List<Locator> categoriesUrls;

    public BrowserManager(boolean headless){ this.headless = headless;}

    public void init(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        context = browser.newContext(new Browser.NewContextOptions().setLocale("es-ES"));
        page = context.newPage();
        navigateToInitialUrl();
    }

    public Page getPage() {
        return this.page;
    }

    public String getUrlBase(){
        return URL_BASE;
    }

    public void navigateToInitialUrl() {
        getPage().navigate(URL_BASE);
    }

    public void scrollPage(int height){
        getPage().mouse().wheel(0,height);
    }

    public void close() {
        this.playwright.close();
    }

    public void navigateTo(String url) {
        getPage().navigate(url);
    }

    public void chargeProductsHtml(int height){
        for (int i = 0; i<5; i++){
            scrollPage(height);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void fill(String inputLocation, String postalCode) {
        page.fill(inputLocation, postalCode);
    }

    public void click(String buttonLocation) {
        page.click(buttonLocation);
    }

    public List<Locator> getCategoriesUrls() {
        return categoriesUrls;
    }

    public void initLocatorList(String htmlReference) {
        categoriesUrls = page.locator(htmlReference).all();
        categoriesUrls = categoriesUrls.subList(LOWER_LIMIT, categoriesUrls.size() - DUPLICATED_MENU_OFFSET);
    }
}
