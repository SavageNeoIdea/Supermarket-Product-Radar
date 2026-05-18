package org.sni.spr.controller;

import java.util.List;

public interface ProductProvider {
    List<String> provideProductIDs(String sitemapUrl);
}
