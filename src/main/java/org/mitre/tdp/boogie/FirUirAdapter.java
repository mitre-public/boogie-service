package org.mitre.tdp.boogie;

import java.util.Optional;
import java.util.function.Function;

import org.mitre.caasd.app.controllers.*;
import org.mitre.caasd.app.controllers.AirspaceSequence;
import org.mitre.caasd.app.controllers.AirspaceType;
import org.mitre.caasd.app.controllers.Geometry;
import org.mitre.caasd.commons.Course;

import com.google.common.collect.Range;

public final class FirUirAdapter implements Function<Airspace, org.mitre.caasd.app.controllers.Airspace> {
  @Override
  public org.mitre.caasd.app.controllers.Airspace apply(Airspace airspace) {
    return new org.mitre.caasd.app.controllers.Airspace(
        airspace.area(),
        airspace.identifier(),
        translate(airspace.airspaceType()),
        airspace.sequences().stream().map(FirUirAdapter::translate).toList())
        .center(airspace.center().map(FirUirAdapter::translate).orElse(null))
        .altitudeLimit(translate(airspace.altitudeLimit()));
  }

  private static AltitudeLimit translate(Range<Double> range) {
    Double min = Optional.of(range).filter(Range::hasLowerBound).map(Range::lowerEndpoint).orElse(null);
    Double max = Optional.of(range).filter(Range::hasUpperBound).map(Range::upperEndpoint).orElse(null);
    return new AltitudeLimit()
        .min(min)
        .max(max);
  }

  private static RouteFix translate(Fix fix) {
    return new RouteFix(fix.fixIdentifier(), fix.latitude(), fix.longitude())
        .magneticVariation(fix.magneticVariation().map(MagneticVariation::angle).map(Course::inDegrees).orElse(null));
  }

  private static AirspaceType translate(org.mitre.tdp.boogie.AirspaceType airspace) {
    return switch (airspace) {
      case FIR -> AirspaceType.FIR;
      case UIR -> AirspaceType.UIR;
      case CONTROLLED -> AirspaceType.CONTROLLED;
      case RESTRICTIVE -> AirspaceType.RESTRICTIVE;
    };
  }

  private static AirspaceSequence translate(org.mitre.tdp.boogie.AirspaceSequence sequence) {
    return new AirspaceSequence(
        translate(sequence.geometry()),
        (double) sequence.sequenceNumber())
        .associatedFix(sequence.associatedFix().map(FirUirAdapter::translate).orElse(null))
        .centerFix(sequence.centerFix().map(FirUirAdapter::translate).orElse(null))
        .arcBearing(sequence.arcBearing().orElse(null))
        .arcRadius(sequence.arcRadius().orElse(null));
  }

  private static LatLong translate(org.mitre.caasd.commons.LatLong latLong) {
    return new LatLong(latLong.latitude(), latLong.longitude());
  }

  private static Geometry translate(org.mitre.tdp.boogie.Geometry geometry) {
    return switch (geometry) {
      case CIRCLE -> Geometry.CIRCLE;
      case CLOCKWISE_ARC -> Geometry.CLOCKWISE_ARC;
      case COUNTER_CLOCKWISE_ARC -> Geometry.COUNTER_CLOCKWISE_ARC;
      case GREAT_CIRCLE -> Geometry.GREAT_CIRCLE;
      case RHUMB_LINE -> Geometry.RHUMB_LINE;
    };
  }

}
