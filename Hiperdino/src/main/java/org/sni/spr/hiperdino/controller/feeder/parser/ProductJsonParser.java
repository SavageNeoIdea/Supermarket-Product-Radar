package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.HiperdinoProduct;

import java.util.List;

public interface ProductJsonParser {
    List<HiperdinoProduct> parse(List<String> rawJson);
}
