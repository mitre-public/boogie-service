package org.mitre.tdp.boogie;

import java.util.List;
import java.util.function.Function;

import org.mitre.caasd.app.controllers.Feature;
import org.mitre.caasd.app.controllers.Point;

public final class AirportGeoJsonAdapter implements Function<Airport, org.mitre.caasd.app.controllers.Feature> {

    private static final AirportAdapter ADAPTER = new AirportAdapter();

    @Override
    public Feature apply(Airport t) {
        List<Double> coordinateArray = List.of(t.latLong().longitude(), t.latLong().latitude());
        Point airportGeometry = new Point(Point.TypeEnum.POINT, coordinateArray);

        return new Feature(Feature.TypeEnum.FEATURE, airportGeometry, ADAPTER.apply(t));
    }
}
