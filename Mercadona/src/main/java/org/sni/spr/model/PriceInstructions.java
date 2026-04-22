package org.sni.spr.model;

import com.google.gson.annotations.SerializedName;

public class PriceInstructions {
    @SerializedName("unit_price")
    String unitPrice;

    @SerializedName("reference_price")
    String referencePrice;

    @SerializedName("unit_size")
    double unitSize;

    @SerializedName("reference_format")
    String referenceFormat;
}
