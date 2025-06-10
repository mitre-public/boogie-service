package org.mitre.tdp.boogie;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.mitre.caasd.app.controllers.Polygon;

public final class AirspaceGeoJsonAdapter implements Function<Airspace, Polygon> {
  @Override
  public Polygon apply(Airspace airspace) {
    List<List<Double>> points = Optional.ofNullable(airspace)
        .map(AirspaceToPoints.INSTANCE)
        .orElse(Collections.emptyList()).stream()
        .map(i -> List.of(i.longitude(), i.latitude()))
        .toList();
    return new Polygon(Polygon.TypeEnum.POLYGON, points);
  }
}
