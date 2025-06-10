package org.mitre.tdp.boogie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.mitre.caasd.commons.LatLong;
import org.mitre.tdp.boogie.projections.Circle;
import org.mitre.tdp.boogie.projections.ClockwiseArc;
import org.mitre.tdp.boogie.projections.CounterClockwiseArc;
import org.mitre.tdp.boogie.projections.GreatCircle;
import org.mitre.tdp.boogie.projections.RhumbLine;
import org.mitre.tdp.boogie.util.Streams;

/**
 * This class converts an airspace into a series of LatLongs. Circles are strange cases because nothing says
 * they can't be in a bigger airspace object, but it does not make sense to do so because there is no way to say where
 * to join the thing.
 */
public final class AirspaceToPoints implements Function<Airspace, List<LatLong>> {
  public static final AirspaceToPoints INSTANCE = new AirspaceToPoints();

  private AirspaceToPoints() {
  }

  private static List<LatLong> forPair(AirspaceSequence first, AirspaceSequence second) {
    return switch (first.geometry()) {
      case CLOCKWISE_ARC ->
          ClockwiseArc.project10Deg(first.associatedFix().orElseThrow(), first.centerFix().orElseThrow(), second.associatedFix().orElseThrow());
      case COUNTER_CLOCKWISE_ARC ->
          CounterClockwiseArc.project10Deg(first.associatedFix().orElseThrow(), first.centerFix().orElseThrow(), second.associatedFix().orElseThrow());
      case GREAT_CIRCLE ->
          GreatCircle.project10NM(first.associatedFix().orElseThrow(), second.associatedFix().orElseThrow());
      case RHUMB_LINE ->
          RhumbLine.project10NM(first.associatedFix().orElseThrow(), second.associatedFix().orElseThrow());
      default ->
          throw new IllegalStateException("Unexpected value: Should not have a circle if there is more than one sequence");
    };
  }

  /***
   * Transforms an airway into a list of LatLongs which includes the first point re-entered as the last point to make
   * geo json polygons happy.
   * @param airspace the airspace
   * @return the list of lat longs
   */
  @Override
  public List<LatLong> apply(Airspace airspace) {
    if (airspace.sequences().size() == 1) {
      return Optional.of(airspace)
          .filter(i -> i.sequences().get(0).geometry().equals(Geometry.CIRCLE))
          .map(i -> Circle.project10Deg(i.sequences().get(0).arcRadius().orElseThrow(), i.sequences().get(0).centerFix().orElseThrow()))
          .orElse(Collections.emptyList());
    }

    List<LatLong> points = new ArrayList<>();
    Streams.pairwise(airspace.sequences()).map(i -> forPair(i.first(), i.second())).forEach(points::addAll);
    points.addAll(forPair(airspace.sequences().get(airspace.sequences().size() - 1), airspace.sequences().get(0)));
    points.add(points.get(0));
    return points;
  }
}
