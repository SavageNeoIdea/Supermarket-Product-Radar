package org.sni.spr.hiperdino.controller.feeder.scraper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoUrlParser;

import java.util.*;
import java.util.function.Consumer;

public class HiperdinoPlaywrightManager implements WebScraper {
    private static final int LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 143;
    private Browser browser;
    private Playwright playwright;
    private Page page;
    private String postalCode;
    private Consumer<List<String>> currentDataConsumer;

    public HiperdinoPlaywrightManager(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public void startScraping(Consumer<List<String>> rawDataConsumer) {
        this.currentDataConsumer = rawDataConsumer;
        initScraperEngine();
        for (String url : extractLinks()) {
            setupWithStreaming(url);
        }
        close();
    }

    private void setupWithStreaming(String url) {
        clickUrlButton(url);
        List<String> context = HiperdinoUrlParser.getCategorySubcategoryName(url);
        String category = context.get(1);
        String subcategory = context.get(2);
        emitFirstProductPage(category, subcategory);
        ProductResponseHandler localHandler = new ProductResponseHandler(page, subcategory, jsonBody -> {
            currentDataConsumer.accept(List.of(jsonBody, category, subcategory));
        });
        localHandler.scrollUntilEnd();
    }

    private void emitFirstProductPage(String category, String subcategory) {
        try {
            page.waitForFunction("() => typeof gtmProductDataObject !== 'undefined'");
            String gtmJson = (String) page.evaluate("() => JSON.stringify(gtmProductDataObject)");
            if (gtmJson != null && !gtmJson.trim().equals("{}")) {
                currentDataConsumer.accept(List.of(gtmJson, category, subcategory));
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo GTM Data inicial: " + e.getMessage());
        }
    }

    private void clickUrlButton(String url) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Locator targetCategory = page.locator(".category-group").filter(new Locator.FilterOptions().setHas(page.locator("a[href*='" + url + "']")))
                .first();
        if (!targetCategory.getAttribute("class").contains("dropdown-open")) {
            targetCategory.locator(".dropdown--trigger").click();
        }
        targetCategory.locator("a[href*='" + url + "']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void initScraperEngine() {
        playwright = Playwright.create();
        this.browser = initBrowser();
        BrowserContext context = initBrowserContext(this.browser);
        initPage(context);
        testWebResponse();
        manageCookies();
        writePostalCode(postalCode);
    }

    private Browser initBrowser() {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList(
                        "--disable-blink-features=AutomationControlled",
                        "--start-maximized"
                )));
    }

    private static BrowserContext initBrowserContext(Browser browser) {
        return browser.newContext(new Browser.NewContextOptions()
                .setLocale("es-ES")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", "es-ES,es;q=0.9",
                        "sec-ch-ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"",
                        "sec-ch-ua-mobile", "?0",
                        "sec-ch-ua-platform", "\"Windows\""
                ))
        );
    }

    private void initPage(BrowserContext context) {
        page = context.newPage();
        page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }

    private void testWebResponse() {
        try {
            page.navigate("https://www.hiperdino.es", new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(90000));
            Thread.sleep(3000 + (int)(Math.random() * 2000));
        } catch (Exception e) {
            extracted(e);
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("error.png")));
        }
    }

    private static void extracted(Exception e) {
        System.err.println("Error en la carga inicial: " + e.getMessage());
    }

    private void manageCookies() {
        String cookieDeclineButton = "button.amgdprcookie-button.-decline";
        click(cookieDeclineButton);
    }

    private void writePostalCode(String postalCode) {
        page.navigate("https://www.hiperdino.es/c9504/alimentacion/aceites.html");
        if (postalCode == null || postalCode.length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 números");
        }
        String textBlock = "input.input__text.required-entry.postal-input";
        String Button = "button[data-myaction='checkCp']";
        fill(textBlock, postalCode);
        click(Button);
    }

    private List<String> extractLinks() {
        List<String> urls = new ArrayList<>();
        String categoriesUrlCssSelector = ".sidebar-item--wrapper a.link--wrapper";
        List<Locator> categoriesUrls = page.
                locator(categoriesUrlCssSelector).all();
        categoriesUrls = categoriesUrls.subList(LOWER_LIMIT, DUPLICATED_MENU_OFFSET-2);
        for (Locator currentLocation : categoriesUrls) {
            String url = currentLocation.getAttribute("href");
            urls.add(url);
        }
        return urls;
    }

    public void close() {
        try {
            if (page != null) {
                page.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            System.err.println("Error durante el cierre del scraper: " + e.getMessage());
        }
    }
    public void fill(String inputLocation, String postalCode) {page.fill(inputLocation, postalCode);}
    public void click(String buttonLocation) {page.click(buttonLocation);}
}
