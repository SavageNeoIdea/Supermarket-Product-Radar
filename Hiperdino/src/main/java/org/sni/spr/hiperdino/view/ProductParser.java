package org.sni.spr.hiperdino.view;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

public interface ProductParser {

    public void identify(String rawName, String rawPrice);

    public String getName();
    public double getPrice();
    public int getPackageQty();
    public int getQty();


    public UnitsOfMeasurement getMeasure();
}
