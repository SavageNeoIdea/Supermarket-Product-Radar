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
        this.productMatcher = productTextpattern.matcher(productCompleteName);
        if (productMatcher.find()) {
            identifyAttrs();
        } else {
            this.name = productCompleteName.trim();
            this.qty = 1;
            this.packageQty = 1;
            this.measure = UnitsOfMeasurement.ud;
        }
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

    @Override
    public String getName() { return name; }
    @Override
    public int getQty() { return qty; }
    @Override
    public UnitsOfMeasurement getMeasure() { return measure; }
    @Override
    public int getPackageQty() { return packageQty; }
}