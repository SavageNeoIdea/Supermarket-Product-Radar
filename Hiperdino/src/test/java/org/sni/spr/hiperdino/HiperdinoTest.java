package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HiperdinoScraperTest {

    private HiperdinoScraper scraper;

    @AfterEach
    void closeContext() {
        scraper.close();
    }

    @Test
    void testManageCookies() {
        scraper = new HiperdinoScraper("35010");
        scraper.init();

        scraper.manageCookies();
        assertFalse(scraper.getPage().isVisible("button.amgdprcookie-button.-decline"),
                "El banner de cookies debería haber desaparecido");
    }

    @Test
    void testValidPostalCode() {
        scraper = new HiperdinoScraper("35010");
        scraper.init();

        scraper.manageCookies();
        boolean result = scraper.writePostalCode();
        assertTrue(result, "El método debería devolver true para un CP con reparto");
    }

    @Test
    void testPostalCodeNotShopAvaibable() {
        HiperdinoScraper fakeScraper = new HiperdinoScraper("00000");
        fakeScraper.init();

        fakeScraper.manageCookies();
        boolean result = fakeScraper.writePostalCode();
        assertFalse(result, "El método debería devolver false cuando aparece el modal de recogida");
    }
}