package org.sni.spr.hiperdino.controller.feeder.parser;

import org.sni.spr.hiperdino.model.UnitsOfMeasurement;

public record ParsedName(String name, int qty, int packageQty, UnitsOfMeasurement measure) {
}