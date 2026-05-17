package org.sni.spr.hiperdino.model;

import java.time.Instant;
import java.util.UUID;

public class HiperdinoProduct {

    private final UUID hiperdinoEventId = UUID.randomUUID();
    private final Instant hiperdinoTs = Instant.now();
    private final String hiperdinoSku;
    private final String hiperdinoEan;
    private final String hiperdinoBrand;
    private final String hiperdinoCategory;
    private final String hiperdinoSubcategory;
    private final String hiperdinoName;
    private final int hiperdinoPackageQty;
    private final int hiperdinoQty;
    private final UnitsOfMeasurement hiperdinoMeasure;
    private final double hiperdinoPrice;
    private final Boolean hiperdinoGluten;
    private final String hiperdinoUrlImage;

    public HiperdinoProduct(
            String hiperdinoSku,
            String hiperdinoEan,
            String hiperdinoBrand,
            String hiperdinoCategory,
            String hiperdinoSubcategory,
            String hiperdinoName,
            int hiperdinoQty,
            int hiperdinoPackageQty,
            UnitsOfMeasurement hiperdinoMeasure,
            double hiperdinoPrice,
            boolean hiperdinoGluten,
            String hiperdinoUrlImage
    ) {

        this.hiperdinoSku = hiperdinoSku;
        this.hiperdinoEan = hiperdinoEan;
        this.hiperdinoBrand = hiperdinoBrand;
        this.hiperdinoCategory = hiperdinoCategory;
        this.hiperdinoSubcategory = hiperdinoSubcategory;
        this.hiperdinoName = hiperdinoName;
        this.hiperdinoQty = hiperdinoQty;
        this.hiperdinoPackageQty = hiperdinoPackageQty;
        this.hiperdinoMeasure = hiperdinoMeasure;
        this.hiperdinoPrice = hiperdinoPrice;
        this.hiperdinoGluten = hiperdinoGluten;
        this.hiperdinoUrlImage = hiperdinoUrlImage;
    }

    @Override
    public String toString() {
        return "HiperdinoProduct{" +
                "hiperdinoEventId=" + hiperdinoEventId +
                ", hiperdinoTs=" + hiperdinoTs +
                ", hiperdinoSku='" + hiperdinoSku + '\'' +
                ", hiperdinoEan='" + hiperdinoEan + '\'' +
                ", hiperdinoBrand='" + hiperdinoBrand + '\'' +
                ", hiperdinoCategory='" + hiperdinoCategory + '\'' +
                ", hiperdinoSubcategory='" + hiperdinoSubcategory + '\'' +
                ", hiperdinoName='" + hiperdinoName + '\'' +
                ", hiperdinoPackageQty=" + hiperdinoPackageQty +
                ", hiperdinoQty=" + hiperdinoQty +
                ", hiperdinoMeasure=" + hiperdinoMeasure +
                ", hiperdinoPrice=" + hiperdinoPrice +
                ", hiperdinoGluten=" + hiperdinoGluten +
                ", hiperdinoUrlImage='" + hiperdinoUrlImage + '\'' +
                '}';
    }

    public UUID getHiperdinoEventId() {
        return hiperdinoEventId;
    }
    public Instant getHiperdinoTs() {
        return hiperdinoTs;
    }
    public String getHiperdinoSku() {
        return hiperdinoSku;
    }
    public String getHiperdinoEan() {
        return hiperdinoEan;
    }
    public String getHiperdinoBrand() {
        return hiperdinoBrand;
    }
    public String getHiperdinoCategory() {
        return hiperdinoCategory;
    }
    public String getHiperdinoSubcategory() {
        return hiperdinoSubcategory;
    }
    public String getHiperdinoName() {
        return hiperdinoName;
    }
    public int getHiperdinoPackageQty() {
        return hiperdinoPackageQty;
    }
    public int getHiperdinoQty() {
        return hiperdinoQty;
    }
    public String getHiperdinoMeasure() {
        return hiperdinoMeasure.name();
    }
    public UnitsOfMeasurement getHiperdinoMeasureEnum() {
        return hiperdinoMeasure;
    }
    public double getHiperdinoPrice() {
        return hiperdinoPrice;
    }
    public Boolean getHiperdinoGluten() {
        return hiperdinoGluten;
    }
    public String getHiperdinoUrlImage() {
        return hiperdinoUrlImage;
    }
}