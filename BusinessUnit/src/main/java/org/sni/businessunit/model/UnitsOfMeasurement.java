package org.sni.businessunit.model;

public enum UnitsOfMeasurement {
    ml(Magnitude.VOLUME, 0.001),
    cl(Magnitude.VOLUME, 0.01),
    l(Magnitude.VOLUME, 1.0),

    g(Magnitude.MASS, 0.001),
    kg(Magnitude.MASS, 1.0),

    cm(Magnitude.LENGTH, 0.01),
    m(Magnitude.LENGTH, 1.0),

    ud(Magnitude.QUANTITY, 1.0),
    uds(Magnitude.QUANTITY, 1.0);

    public enum Magnitude { VOLUME, MASS, LENGTH, QUANTITY }

    private final Magnitude magnitude;
    private final double factorToSI;

    UnitsOfMeasurement(Magnitude magnitude, double factorToSI) {
        this.magnitude = magnitude;
        this.factorToSI = factorToSI;
    }

    public Magnitude getMagnitude() { return magnitude; }
    public double getFactorToSI() { return factorToSI; }
}