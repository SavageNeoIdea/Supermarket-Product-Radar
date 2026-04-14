package org.sni.spr.hiperdino.view;
import java.util.concurrent.ThreadLocalRandom;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.*;

public class HiperdinoPlaywrightManager implements WebScraper {
    private static final int LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 139;

    private Playwright playwright;
    private Page page;

    @Override
    public void init(String postalCode) {
        playwright = Playwright.create();
        Browser browser = initBrowser();
        BrowserContext context = initBrowserContext(browser);
        initPage(context);
        testWebResponse();
        manageCookies();
        navigateToLikeAClick("https://www.hiperdino.es/c9504/alimentacion/aceites.html",
                "https://www.hiperdino.es");
        writePostalCode(postalCode);

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

    private static BrowserContext initBrowserContext(Browser browser) {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
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
        return context;
    }

    private Browser initBrowser() {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList(
                        "--disable-blink-features=AutomationControlled", // Clave para ocultar Playwright
                        "--start-maximized"
                )));
    }

    @Override
    public Map<String, List<Map<String, String>>> extractProductRawData() {
        List<String> urls = extractLinks();
        Map<String, List<Map<String, String>>> productMap = new LinkedHashMap<>();

        String lastUrl = "https://www.hiperdino.es/";

        for (int i = 0; i < urls.size(); i++) {
            String currentUrl = urls.get(i);

            System.out.println("Navegando a: " + currentUrl + " (Viniendo de: " + lastUrl + ")");
            navigateToLikeAClick(currentUrl, lastUrl);
            humanWait(4000, 9000);
            scrollUntilEnd();

            List<Map<String, String>> categoryProducts = new ArrayList<>();
            List<Locator> productLocators = page.locator("li.product-list-item .product-container").all();
            List<String> context = HiperdinoUrlParser.getCategorySubcategoryName(currentUrl);

            for (Locator product : productLocators) {
                Map<String, String> details = scrapeProductData(product, context);
                categoryProducts.add(details);

                if (categoryProducts.size() % 10 == 0) {
                    humanWait(300, 600);
                }
            }

            if (!context.isEmpty()) {
                productMap.put(context.getFirst(), categoryProducts);
            }

            lastUrl = currentUrl;

            if (i < urls.size() - 1) { // No esperar 6 minutos en la última categoría
                System.out.println("Categoría " + (i + 1) + "/" + urls.size() + " finalizada. Pausa de seguridad...");
                humanWait(120000, 360000);
            }
        }

        return productMap;
    }

    private Map<String, String> scrapeProductData(Locator product, List<String> context) {
        Map<String, String> details = new HashMap<>();

        String name = product.locator(".name").innerText().trim();
        String price = product.locator(".price").innerText().trim();
        String urlImage = product.locator("img").getAttribute("src");

        boolean noGluten = product.locator("img[alt*='Sin Gluten']").count() > 0 ||
                product.innerText().toLowerCase().contains("sin gluten");

        details.put("name", name);
        details.put("price", price);
        details.put("category", context.get(1));
        details.put("subcategory", context.get(2));
        details.put("gluten", String.valueOf(noGluten));
        details.put("urlImage", urlImage);

        return details;
    }

    private void manageCookies() {
        String cookieDeclineButton = "button.amgdprcookie-button.-decline";
        click(cookieDeclineButton);
    }

    private void writePostalCode(String postalCode) {

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
        //This is for test without exploring all the web, real subList is: categoriesUrls.size() - DUPLICATED_MENU_OFFSET
        categoriesUrls = categoriesUrls.subList(LOWER_LIMIT, 18);

        for (Locator currentLocation : categoriesUrls) {
            String url = currentLocation.getAttribute("href");
            urls.add(url);
        }
        return urls;
    }

    private void scrollUntilEnd() {
        String productSelector = ".product-list-item.flex-item.loader-over";
        int attempts = 0;
        int maxAttempts = 6;

        while (attempts < maxAttempts) {
            int currentCount = page.locator(productSelector).count();

            if (currentCount > 0) {
                page.locator(productSelector).last().scrollIntoViewIfNeeded();
            } else {
                chargeProductsHtml();
            }

            humanWait(2000, 4500);

            int newCount = page.locator(productSelector).count();

            if (newCount > currentCount) {
                attempts = 0;
            } else {
                attempts++;
                chargeProductsHtml();
            }
        }
    }

    private void humanWait(int min, int max) {
        try {
            long sleepTime = ThreadLocalRandom.current().nextLong(min, max + 1);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error en la espera aleatoria", e);
        }
    }

    private void chargeProductsHtml() {
        for (int i = 0; i < 3; i++) {
            int scrollAmount = ThreadLocalRandom.current().nextInt(400, 900);
            scrollPage(scrollAmount);
            humanWait(400, 800);
        }
    }

    public void navigateToLikeAClick(String url,String lastUrl) {
        Map<String, String> clickHeaders = new HashMap<>();
        clickHeaders.put("Referer", lastUrl);
        clickHeaders.put("Sec-Fetch-Dest", "document");
        clickHeaders.put("Sec-Fetch-Mode", "navigate");
        clickHeaders.put("Sec-Fetch-Site", "same-origin");
        clickHeaders.put("Upgrade-Insecure-Requests", "1");

        page.setExtraHTTPHeaders(clickHeaders);

        page.navigate(url, new Page.NavigateOptions());
    }

    @Override
    public void navigateTo(String url) {
        getPage().navigate(url);
    }
    @Override
    public Page getPage() {
        return this.page;
    }
    @Override
    public void scrollPage(int height) {
        getPage().mouse().wheel(0, height);
    }
    @Override
    public void close() {
        this.playwright.close();
    }
    @Override
    public void fill(String inputLocation, String postalCode) {
        page.fill(inputLocation, postalCode);
    }
    @Override
    public void click(String buttonLocation) {
        page.click(buttonLocation);
    }
}
