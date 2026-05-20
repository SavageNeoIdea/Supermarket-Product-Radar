package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoProductNameParser implements ProductNameParser {

    private static final Pattern PRODUCT_TEXT_PATTERN = Pattern.compile(
            "^(.*?)(?:\\s+(?:(\\d+)\\s*[xX]\\s*)?(\\d+)\\s*(ml|cl|l|g|kg|ud|uds\\.?)|\\s+(cm))$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public ParsedName parse(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return createDefaultFallback(rawName);
        }
        Matcher matcher = PRODUCT_TEXT_PATTERN.matcher(rawName);
        if (!matcher.find()) {
            return createDefaultFallback(rawName);
        }
        return buildParsedName(matcher);
    }

    private ParsedName buildParsedName(Matcher matcher) {
        String name = matcher.group(1).trim();

        if (matcher.group(5) != null) {
            return new ParsedName(name, 1, 1, UnitsOfMeasurement.cm);
        }
        int baseQty = Integer.parseInt(matcher.group(3));
        UnitsOfMeasurement measure = mapToUnitsOfMeasurement(matcher.group(4));
        int packageQty = (matcher.group(2) != null) ? Integer.parseInt(matcher.group(2)) : 1;
        int totalQty = baseQty * packageQty;

        return new ParsedName(name, totalQty, packageQty, measure);
    }

    private UnitsOfMeasurement mapToUnitsOfMeasurement(String rawUnit) {
        if (rawUnit == null) return UnitsOfMeasurement.ud;
        String cleanUnit = rawUnit.replace(".", "").trim().toLowerCase();
        return UnitsOfMeasurement.valueOf(cleanUnit);
    }

    private ParsedName createDefaultFallback(String rawName) {
        String safeName = (rawName != null) ? rawName.trim() : "PRODUCTO DESCONOCIDO";
        return new ParsedName(safeName, 1, 1, UnitsOfMeasurement.ud);
    }
}