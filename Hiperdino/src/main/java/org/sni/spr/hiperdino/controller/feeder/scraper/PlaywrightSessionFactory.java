package org.sni.spr.hiperdino.controller.feeder.scraper;

import com.microsoft.playwright.*;
import java.util.*;

public class PlaywrightSessionFactory implements AutoCloseable {
    private Playwright playwright;
    private Browser browser;

    public Page createPage() {
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

    @Override
    public void close() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}