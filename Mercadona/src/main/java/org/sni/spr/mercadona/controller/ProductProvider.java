package org.sni.spr.mercadona.controller;

import java.util.List;

public interface ProductProvider {
    List<String> provideProductIDs(String sitemapUrl);
}
