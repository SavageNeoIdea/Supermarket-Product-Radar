package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.HiperdinoProduct;
import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

import java.util.List;

public interface ProductNameParser {

    public void identify(String rawName);
    public String getName();
    public int getPackageQty();
    public int getQty();
    public UnitsOfMeasurement getMeasure();
}
