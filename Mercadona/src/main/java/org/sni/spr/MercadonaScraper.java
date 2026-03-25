package org.sni.spr;

import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import java.util.List;

public class MercadonaScraper {
    public static void main(String[] args) throws InterruptedException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().
                    launch(new LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setLocale("es-ES"));
            Page page = context.newPage();
            String postalCode = "35010";

            page.navigate("https://www.mercadona.es/");
            page.waitForSelector("input[name='postcode']");
            page.waitForSelector(".postal-code-form__button");
            page.fill("input[aria-label='Código postal']", postalCode);
            page.click(".postal-code-form__button");
            Thread.sleep(10000);
        }
    }
}