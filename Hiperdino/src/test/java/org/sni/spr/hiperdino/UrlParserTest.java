package org.sni.spr.hiperdino;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UrlParserTest {

    @Test
    void testGetFormatedDataWithValidUrl() {

        Pattern pattern = UrlParser.initPattern();
        String url = "https://www.hiperdino.es/c9504/alimentacion/aceites-y-vinagres.html";
        Matcher matcher = pattern.matcher(url);

        List<String> result = UrlParser.getFormatedData(matcher, url);

        assertFalse(result.isEmpty(), "La lista no debería estar vacía con una URL válida");
        assertEquals(4, result.size(), "El tamaño de la lista de datos formateados debe ser 4");

        assertEquals("alimentacion - aceites y vinagres", result.get(0));
        assertEquals(url, result.get(1));
        assertEquals("alimentacion", result.get(2));
        assertEquals("aceites y vinagres", result.get(3));
    }

    @Test
    void testGetFormatedDataWithInvalidUrl() {

        Pattern pattern = UrlParser.initPattern();
        String url = "https://www.hiperdino.es/otra-ruta/invalida.html";
        Matcher matcher = pattern.matcher(url);

        List<String> result = UrlParser.getFormatedData(matcher, url);

        assertTrue(result.isEmpty(), "La lista debería estar vacía si la URL no hace match");
    }
}