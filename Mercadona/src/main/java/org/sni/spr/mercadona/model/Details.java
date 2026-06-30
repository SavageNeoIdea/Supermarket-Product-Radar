package org.sni.spr.mercadona.model;

import com.google.gson.annotations.SerializedName;

public class Details {
    @SerializedName("mandatory_mentions")
    private String mandatoryMentions;

    public String getMandatoryMentions() {
        return mandatoryMentions;
    }
}