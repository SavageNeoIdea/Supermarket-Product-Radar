package org.sni.spr.mercadona.model;

import com.google.gson.annotations.SerializedName;

public class PriceInstructions {
    @SerializedName("unit_price")
    String unitPrice;

    @SerializedName("unit_size")
    double unitSize;

    @SerializedName("size_format")
    String sizeFormat;

    @SerializedName("total_units")
    int totalUnits;
}
