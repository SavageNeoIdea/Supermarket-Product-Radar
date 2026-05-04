package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoProductParser implements ProductParser {

    private static final Pattern productTextpattern = Pattern.compile("^(.*)\\s+(\\d+x)?(\\d+)\\s*(ml|cl|l|g|kg|ud|uds)$");
    //private static final Pattern pricePattern = Pattern.compile("(\\d+,\\d+)");

    private String text;
    private String priceText;

    private String name;
    // private double price;
    private int qty;
    private UnitsOfMeasurement measure;
    private int packageQty;

    private Matcher productMatcher;
    private Matcher priceMatcher;

    @Override
    public void identify(String text){
        this.text = text;
        //this.priceText = priceText;
        initMatcher();
        if (productMatcher.find()) {
            identifyAttrs();
            updateQtyBasedOnPackage();
        }
    }

    public void initMatcher() {
        productMatcher = productTextpattern.matcher(text);
        //priceMatcher = pricePattern.matcher(priceText);
    }

    private void identifyAttrs() {
        name = productMatcher.group(1).trim();
        qty = Integer.parseInt(productMatcher.group(3).trim());
        measure = UnitsOfMeasurement.valueOf(productMatcher.group(4));
        /*
        if (priceMatcher.find()) {
            price = Double.parseDouble(priceMatcher.group(1).replace(",", "."));
        }*/
    }

    private boolean identifyPackage() {
        if (productMatcher.group(2) != null) {
            String packageQtyNotFormated = productMatcher.group(2);
            this.packageQty = Integer.parseInt(packageQtyNotFormated.substring(0, packageQtyNotFormated.length() - 1));
            return true;
        } return false;
    }

    private void updateQtyBasedOnPackage() {
        if (identifyPackage()) {
            qty = packageQty * qty;
        } else packageQty = 1;
    }

    public String getText() { return text; }
   // public String getPriceText() { return priceText; }
    public String getName() { return name; }
    //public double getPrice() { return this.price; }
    public int getQty() { return qty; }
    public UnitsOfMeasurement getMeasure() { return measure; }

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

    public int getPackageQty() { return packageQty; }
}
