package org.sni.spr.hiperdino;
import com.microsoft.playwright.*;

public class HiperdinoScraper {

    private static final String URL_BASE = "https://www.hiperdino.es/c9504/alimentacion/aceites.html";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final String postalCode;

    public HiperdinoScraper(String postalCode) {
        this.postalCode = postalCode;
    }

    public Page getPage() {
        return this.page;
    }

    public void init(){
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        this.context = browser.newContext(new Browser.NewContextOptions().setLocale("es-ES"));
        this.page = this.context.newPage();
        this.page.navigate(URL_BASE);
}

    public void manageCookies(){
        this.page.click("button.amgdprcookie-button.-decline");
    }

    public boolean writePostalCode() {
        this.page.fill( "input.input__text.required-entry.postal-input", this.postalCode);
        this.page.click("button[data-myaction='checkCp']");
        return !this.page.isVisible("span:has-text('Elige una Isla')");
    }

    public void close(){
        this.playwright.close();
    }

    /*
    private void dropdownCategory(String category){}

    private void extractSubCategories(String category){}
    */
}