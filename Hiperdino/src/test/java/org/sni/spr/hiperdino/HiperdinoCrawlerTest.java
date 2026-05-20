/*package org.sni.spr.hiperdino;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HiperdinoCrawlerTest {

    @Test
    void testConstructorValidPostalCode() {

        BrowserManager mockBrowserManager = new BrowserManager(true);
        assertDoesNotThrow(() -> new HiperdinoCrawler("35010", mockBrowserManager));
    }

    @Test
    void testConstructorInvalidPostalCodeThrowsException() {
        BrowserManager mockBrowserManager = new BrowserManager(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HiperdinoCrawler("123", mockBrowserManager);
        });

        assertEquals("El código postal debe tener 5 números", exception.getMessage());
    }
}*/