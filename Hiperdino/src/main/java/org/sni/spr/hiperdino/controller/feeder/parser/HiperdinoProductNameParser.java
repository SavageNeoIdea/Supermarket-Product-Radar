package org.sni.spr.hiperdino.controller.feeder.parser;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiperdinoProductNameParser implements ProductNameParser {

    private static final Pattern productTextpattern = Pattern.compile(
            "^(.*?)(?:\\s+(?:(\\d+)\\s*[xX]\\s*)?(\\d+)\\s*(ml|cl|l|g|kg|ud|uds\\.?)|\\s+(cm))$",
            Pattern.CASE_INSENSITIVE
    );

    private String name;
    private int qty;
    private UnitsOfMeasurement measure;
    private int packageQty;
    private Matcher productMatcher;

    @Override
    public void identify(String productCompleteName) {
        Matcher productMatcher = calculateProductMatcher(productCompleteName);
        if (productMatcher.find()) {
            identifyAttrs();
        }
    }

    private Matcher calculateProductMatcher(String productCompleteName) {
        return productTextpattern.matcher(productCompleteName);
    }

    private void identifyAttrs() {
        name = calculateName();
        if (productHaveCm()) {
            setProductCmDefaultData();
        } else {
            processStandardProduct();
        }
    }

    private void processStandardProduct() {
        qty = calculateQty();
        measure = calculateMeasure();
        packageQty = productHavePackage() ? calculatePackageQty() : 1;
        qty = packageQty * qty;
    }

    private String calculateName() {
        return productMatcher.group(1).trim();
    }

    private int calculatePackageQty() {
        return Integer.parseInt(productMatcher.group(2));
    }

    private boolean productHavePackage() {
        return productMatcher.group(2) != null;
    }

    private UnitsOfMeasurement calculateMeasure() {
        String rawUnit = productMatcher.group(4).replace(".", "");
        return UnitsOfMeasurement.valueOf(rawUnit);
    }

    private int calculateQty() {
       return Integer.parseInt(productMatcher.group(3));
    }

    private void setProductCmDefaultData() {
        qty = 1;
        packageQty = 1;
        measure = UnitsOfMeasurement.valueOf("cm");
    }

    private boolean productHaveCm(){
        return productMatcher.group(5) != null;
    }

    @Override
    public String getName() { return name; }
    @Override
    public int getQty() { return qty; }
    @Override
    public UnitsOfMeasurement getMeasure() { return measure; }
    @Override
    public int getPackageQty() { return packageQty; }
}