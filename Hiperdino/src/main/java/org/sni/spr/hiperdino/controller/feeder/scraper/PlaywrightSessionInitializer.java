package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.*;

import java.util.Arrays;
import java.util.Map;

public class PlaywrightSessionInitializer implements AutoCloseable{
    private final String postalCode;
    private Playwright playwright;
    private Browser browser;

    public PlaywrightSessionInitializer(String postalCode) {
        validatePostalCode(postalCode);
        this.postalCode = postalCode;
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.length() != 5) {
            throw new IllegalArgumentException("Código postal inválido");
        }
    }

    public Page setupHiperdinoPage() {
        Page page = createPage();
        openHiperdinoPage(page);
        declineCookies(page);
        enterPostalCode(page);
        return page;
    }
    private Page createPage() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList("--disable-blink-features=AutomationControlled", "--start-maximized")));

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setLocale("es-ES")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...")
                .setViewportSize(1920, 1080)
                .setExtraHTTPHeaders(Map.of("Accept-Language", "es-ES,es;q=0.9")));

        Page page = context.newPage();
        page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        return page;
    }

    private void openHiperdinoPage(Page page){
        page.navigate("https://www.hiperdino.es/c9504/alimentacion/aceites.html");
        page.waitForTimeout(3000 + (Math.random() * 2000));
    }

    private void declineCookies(Page page){
        page.click("button.amgdprcookie-button.-decline");
    }

    private void enterPostalCode(Page page){
        page.fill("input.input__text.required-entry.postal-input", postalCode);
        page.click("button[data-myaction='checkCp']");
    }

    @Override
    public void close() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}