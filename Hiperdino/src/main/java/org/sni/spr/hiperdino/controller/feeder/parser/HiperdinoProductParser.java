package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoProductParser implements ProductParser {

    private static final Pattern productTextpattern = Pattern.compile(
            "^(.*?)(?:\\s+(?:(\\d+)\\s*[xX]\\s*)?(\\d+)\\s*(ml|cl|l|g|kg|ud|uds\\.?)|\\s+(cm))$",
            Pattern.CASE_INSENSITIVE
    );

    private String text;
    private String name;
    private int qty;
    private UnitsOfMeasurement measure;
    private int packageQty;
    private Matcher productMatcher;

    @Override
    public void identify(String text){
        this.text = text;
        initMatcher();
        if (productMatcher.find()) {
            identifyAttrs();
        }
    }

    public void initMatcher() {
        productMatcher = productTextpattern.matcher(text);
    }

    @Override
    public double getRawPriceAsDouble(String priceStr) {
        double price = 0.0;
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr.replace(",", "."));
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir precio: " + priceStr);
            }
        }
        return price;
    }

    private void identifyAttrs() {
        name = productMatcher.group(1).trim();

        if (productMatcher.group(5) != null) {
            qty = 1;
            packageQty = 1;
            measure = UnitsOfMeasurement.valueOf("cm");
        } else {
            qty = Integer.parseInt(productMatcher.group(3));
            String rawUnit = productMatcher.group(4).replace(".", "");
            measure = UnitsOfMeasurement.valueOf(rawUnit);

            if (productMatcher.group(2) != null) {
                packageQty = Integer.parseInt(productMatcher.group(2));
                qty = packageQty * qty;
            } else {
                packageQty = 1;
            }
        }
    }

    public String getName() { return name; }
    public int getQty() { return qty; }
    public UnitsOfMeasurement getMeasure() { return measure; }
    public int getPackageQty() { return packageQty; }
}