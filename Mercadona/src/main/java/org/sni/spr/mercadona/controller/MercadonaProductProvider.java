package org.sni.spr.mercadona.controller;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.util.List;

public class MercadonaProductProvider implements ProductProvider {

    @Override
    public List<String> provideProductIDs(String sitemapUrl) {
        try {
            return Jsoup.connect(sitemapUrl)
                    .parser(Parser.xmlParser())
                    .get()
                    .select("loc")
                    .eachText()
                    .stream()
                    .filter(url -> url.contains("/product/"))
                    .map(url -> url.split("/"))
                    .map(parts -> parts[parts.length - 2])
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error reading sitemap", e);
        }
    }
}