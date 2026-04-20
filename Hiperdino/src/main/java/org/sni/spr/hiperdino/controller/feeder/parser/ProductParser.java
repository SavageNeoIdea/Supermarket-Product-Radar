package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

public interface ProductParser {

    public void identify(String rawName);
    public String getName();
    public int getPackageQty();
    public int getQty();
    public UnitsOfMeasurement getMeasure();
}
