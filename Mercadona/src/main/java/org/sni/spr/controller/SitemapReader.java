package org.sni.spr.controller;

import javax.xml.stream.*;
import java.net.URI;
import java.util.*;

public class SitemapReader {

    public List<String> readSitemap(String sitemapUrl) {
        List<String> urls = new ArrayList<>();
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new URI(sitemapUrl).toURL().openStream());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT &&
                        "loc".equals(reader.getLocalName())) {
                    urls.add(reader.getElementText().trim());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading sitemap", e);
        }
        return urls;
    }
}