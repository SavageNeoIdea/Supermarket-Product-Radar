package org.sni.spr.hiperdino.controller.feeder.parser;

public record ScraperRawPayload(String jsonBody, String category, String subcategory) {
}