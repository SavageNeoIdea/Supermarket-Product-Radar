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

            page.waitForSelector("input[aria-label='Código postal']").fill(postalCode);
            page.locator("input.postal-code-form__button[value='ENTRAR']").first().click();
            page.waitForLoadState();

            page.getByText("Categorías").first().click();


            Thread.sleep(7000);
            page.close();
        }
    }
}