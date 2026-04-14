package org.sni.spr.hiperdino.model;

import java.time.LocalDateTime;

public interface Product {

    public String getName();
    String getCategory();
    String getSubcategory();
    public double getPrice();
    public int getPackageQty();
    public int getQty();
    public UnitsOfMeasurement getMeasure();
    Boolean getGluten();
    LocalDateTime getNow();
    String getUrlImage();


}
