package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.sni.spr.hiperdino.controller.feeder.parser.HiperdinoUrlParser;

import java.util.*;

public class HiperdinoPlaywrightManager implements WebScraper {
    private static final int LOWER_LIMIT = 13;
    private static final int DUPLICATED_MENU_OFFSET = 143;

    private Playwright playwright;
    private Page page;
    private String postalCode;
    public HiperdinoPlaywrightManager(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public List<Map<String, String>> extractProductRawData() {
        init();
        List<String> urls = extractLinks();
        List<Map<String, String>> allProducts = new ArrayList<>();

        for (String url : urls) {
            List<String> context = HiperdinoUrlParser.getCategorySubcategoryName(url);
            clickUrlButton(url);
            allProducts.addAll(extractGtmProducts(context));
            ProductResponseHandler deserializer = new ProductResponseHandler(page, context.getLast());
            deserializer.setupNetworkInterceptor();

            deserializer.scrollUntilEnd();

            List<String> capturedJsons = deserializer.getCapturedResponses();

            List<Locator> productLocators = page.locator(".product-list-item").all();

            for (Locator productLocator : productLocators) {
                Map<String, String> rawData = createRawMap(productLocator, context, capturedJsons);

                if (!rawData.isEmpty()) {
                    allProducts.add(rawData);
                }
            }
        }
        close();
        return allProducts;
    }

    private List<Map<String, String>> extractGtmProducts(List<String> context) {
        List<Map<String, String>> gtmProducts = new ArrayList<>();
        try {
            // 1. Esperar a que la variable esté lista en el navegador
            page.waitForFunction("() => typeof gtmProductDataObject !== 'undefined'");

            // 2. Obtener el JSON string del objeto global
            String gtmJson = (String) page.evaluate("() => JSON.stringify(gtmProductDataObject)");

            // 3. Procesar con Jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(gtmJson);

            root.fields().forEachRemaining(entry -> {
                JsonNode p = entry.getValue();
                Map<String, String> map = new HashMap<>();

                map.put("sku", p.path("sku").asText());
                map.put("ean", p.path("ean").asText());
                map.put("name", p.path("name").asText());
                map.put("price", p.path("final_price").asText());
                map.put("brand", p.path("label_brand").asText());
                map.put("sap_id", p.path("entity_id").asText());
                map.put("image_url", "https://www.hiperdino.es/media/catalog/product/" + p.path("image").asText());
                if (context.size() >= 3) {
                    map.put("category", context.get(1));
                    map.put("subcategory", context.get(2));
                }
                gtmProducts.add(map);
            });

        } catch (Exception e) {
            System.err.println("Error procesando GTM Data: " + e.getMessage());
        }
        return gtmProducts;
    }

    private Map<String, String> createRawMap(Locator product, List<String> context, List<String> jsons) {
        Map<String, String> map = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        String productId = product.getAttribute("data-id");
        if (productId == null) return map;
        for (String jsonStr : jsons) {
            try {
                JsonNode root = mapper.readTree(jsonStr);
                JsonNode gtmData = root.path("productGtmData");

                if (gtmData.has(productId)) {
                    JsonNode p = gtmData.get(productId);
                    map.put("sku", p.path("sku").asText());
                    map.put("ean", p.path("ean").asText());
                    map.put("name", p.path("name").asText());
                    map.put("price", p.path("final_price").asText());
                    map.put("sap_id", p.path("entity_id").asText());
                    map.put("image_url", "https://www.hiperdino.es/media/catalog/product" + p.path("image").asText());
                    map.put("category", context.get(1));
                    map.put("subcategory", context.get(2));
                    return map;
                }
            } catch (Exception e) {}
        }
        return map;
    }

    private void clickUrlButton(String url) {
        Locator targetCategory = page.locator(".category-group").filter(new Locator.FilterOptions().setHas(page.locator("a[href*='" + url + "']")))
                .first();
        if (!targetCategory.getAttribute("class").contains("dropdown-open")) {
            targetCategory.locator(".dropdown--trigger").click();
        }
        targetCategory.locator("a[href*='" + url + "']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void init() {
        playwright = Playwright.create();
        Browser browser = initBrowser();
        BrowserContext context = initBrowserContext(browser);
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

    public void close() {this.playwright.close();}
    public void fill(String inputLocation, String postalCode) {page.fill(inputLocation, postalCode);}
    public void click(String buttonLocation) {page.click(buttonLocation);}
}
