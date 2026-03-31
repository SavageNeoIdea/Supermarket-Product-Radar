package org.sni.spr.hiperdino;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HiperdinoScraperTest {

    private HiperdinoScraper scraper;

    @BeforeEach
    void setup() {
        scraper = new HiperdinoScraper("35010");
        scraper.init();
    }

    @AfterEach
    void closeContext() {
        scraper.close();
    }

    @Test
    void testManageCookies() {

        scraper.manageCookies();
        assertFalse(scraper.getPage().isVisible("button.amgdprcookie-button.-decline"),
                "El banner de cookies debería haber desaparecido");
    }

    @Test
    void testValidPostalCode() {
        scraper.manageCookies();
        boolean result = scraper.writePostalCode();
        assertTrue(result, "El método debería devolver true para un CP con reparto");
    }

    @Test
    void testBadPostalCodeLength() {
        Throwable exception = assertThrowsExactly(IllegalArgumentException.class, () -> {
            new HiperdinoScraper("000000");
        });

        assertEquals("El código postal debe tener 5 números", exception.getMessage());
    }

    @Test
    void testPostalCodeNotShopAvaibable() {
        HiperdinoScraper fakeScraper = new HiperdinoScraper("00000");
        fakeScraper.init();

        fakeScraper.manageCookies();
        boolean result = fakeScraper.writePostalCode();
        assertFalse(result, "El método debería devolver false cuando aparece el modal de recogida");
    }

    @Test
    void testExtractCategoriesNotEmpty() {
        Map<String, String> categories = scraper.extractCategories();
        assertNotNull(categories, "El mapa de categorías no debería ser null");
        assertFalse(categories.isEmpty(), "Debería haber extraído al menos una categoría");
    }

    @Test
    void testUrlsAreWellFormed() {
        Map<String, String> categories = scraper.extractCategories();
        String firstUrl = categories.values().iterator().next();

        assertTrue(firstUrl.startsWith("https://www.hiperdino.es"),
                "Las URLs deben empezar por el dominio de Hiperdino");
        assertTrue(firstUrl.endsWith(".html"),
                "Las URLs de las categorías deben terminar en .html");
    }
}