package org.sni.spr.hiperdino;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductIdentifier {

    private static final Pattern productTextpattern = Pattern.compile("^(.*)\\s+(\\d+x)?(\\d+)\\s*(ml|cl|l|g|kg)$");
    private static final Pattern pricePattern = Pattern.compile("(\\d+,\\d+)");

    private final String text;
    private final String priceText;

    private String name;
    private double price;
    private int qty;
    private UnitsOfMeasurement measure;
    private int packageQty;

    private Matcher productMatcher;
    private Matcher priceMatcher;

    public ProductIdentifier(String text, String priceText) {
        this.text = text;
        this.priceText = priceText;
        initMatcher();
        if (this.productMatcher.find()) {
            identifyAttrs();
            updateQtyBasedOnPackage();
        }
    }

    public void initMatcher() {
        this.productMatcher = productTextpattern.matcher(text);
        this.priceMatcher = pricePattern.matcher(priceText);
    }

    private void identifyAttrs() {
        this.name = productMatcher.group(1).trim();
        this.qty = Integer.parseInt(productMatcher.group(3).trim());
        this.measure = UnitsOfMeasurement.valueOf(productMatcher.group(4));

        if (priceMatcher.find()) {
            this.price = Double.parseDouble(priceMatcher.group(1).replace(",", "."));
        }
    }


    private boolean identifyPackage() {
        if (productMatcher.group(2) != null) {
            String packageQtyNotFormated = productMatcher.group(2);
            this.packageQty = Integer.parseInt(packageQtyNotFormated.substring(0, packageQtyNotFormated.length() - 1));
        } return false;
    }

    private void updateQtyBasedOnPackage() {
        if (identifyPackage()) {
            qty = packageQty * qty;
        }
    }

    public String getText() { return text; }
    public String getPriceText() { return priceText; }
    public String getName() { return name; }
    public double getPrice() { return this.price; }
    public int getQty() { return qty; }
    public UnitsOfMeasurement getMeasure() { return measure; }
    public int getPackageQty() { return packageQty; }
}
