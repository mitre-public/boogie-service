package org.mitre.tdp.boogie;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.Range;
import org.mitre.caasd.app.controllers.Equipage;
import org.mitre.caasd.app.controllers.ExpandedRoute;
import org.mitre.caasd.app.controllers.ExpandedRouteLeg;
import org.mitre.caasd.app.controllers.PathTerminator;
import org.mitre.caasd.app.controllers.RouteConstraint;
import org.mitre.caasd.app.controllers.RouteFix;
import org.mitre.caasd.app.controllers.RouteLeg;
import org.mitre.caasd.app.controllers.RouteSummary;
import org.mitre.caasd.app.controllers.TurnDirection;
import org.mitre.caasd.commons.Course;

final class ExpandedRouteAdapter implements Function<org.mitre.tdp.boogie.alg.facade.ExpandedRoute, ExpandedRoute> {

  ExpandedRouteAdapter() {
  }

  @Override
  public ExpandedRoute apply(org.mitre.tdp.boogie.alg.facade.ExpandedRoute route) {

    Optional<RouteSummary> summary = route.routeSummary().map(ExpandedRouteAdapter::translate);

    List<ExpandedRouteLeg> legs = route.legs().stream()
        .map(this::translate)
        .toList();

    return new ExpandedRoute(legs).routeSummary(summary.orElse(null));
  }

  static RouteSummary translate(org.mitre.tdp.boogie.alg.facade.RouteSummary summary) {
    return new RouteSummary(summary.route())
        .departureAirport(summary.departureAirport())
        .departureRunway(summary.departureRunway().orElse(null))
        .departureFix(summary.departureFix().orElse(null))
        .sid(summary.sid().orElse(null))
        .sidExitFix(summary.sidExitFix().orElse(null))
        .requiredSidEquipage(summary.requiredSidEquipage().map(e -> Equipage.valueOf(e.name())).orElse(null))
        .arrivalAirport(summary.arrivalAirport())
        .arrivalRunway(summary.arrivalRunway().orElse(null))
        .arrivalFix(summary.arrivalFix().orElse(null))
        .star(summary.star().orElse(null))
        .starEntryFix(summary.starEntryFix().orElse(null))
        .requiredStarEquipage(summary.requiredStarEquipage().map(e -> Equipage.valueOf(e.name())).orElse(null))
        .approach(summary.approach().orElse(null))
        .approachEntryFix(summary.approachEntryFix().orElse(null))
        .requiredApproachEquipage(summary.requiredApproachEquipage().map(e -> Equipage.valueOf(e.name())).orElse(null));
  }

  private ExpandedRouteLeg translate(org.mitre.tdp.boogie.alg.facade.ExpandedRouteLeg leg) {

    RouteLeg routeLeg = createRouteLeg(leg)
        .associatedFix(leg.associatedFix().map(ExpandedRouteAdapter::translate).orElse(null))
        .recommendedNavaid(leg.recommendedNavaid().map(ExpandedRouteAdapter::translate).orElse(null))
        .centerFix(leg.centerFix().map(ExpandedRouteAdapter::translate).orElse(null))
        .outboundMagneticCourse(leg.outboundMagneticCourse().orElse(null))
        .rho(leg.rho().orElse(null))
        .theta(leg.theta().orElse(null))
        .rho(leg.rho().orElse(null))
        .routeDistance(leg.routeDistance().orElse(null))
        .holdTime(leg.holdTime().map(Duration::toMinutes).map(Long::intValue).orElse(null))
        .verticalAngle(leg.verticalAngle().orElse(null))
        .speedConstraint(translate(leg.speedConstraint()))
        .altitudeConstraint(translate(leg.altitudeConstraint()))
        .turnDirection(leg.turnDirection().map(ExpandedRouteAdapter::translate).orElse(null));

    return new ExpandedRouteLeg(leg.section(), leg.elementType().name(), routeLeg);
  }

  private RouteLeg createRouteLeg(org.mitre.tdp.boogie.alg.facade.ExpandedRouteLeg leg) {
    return new RouteLeg(
        PathTerminator.valueOf(leg.pathTerminator().name()),
        leg.sequenceNumber(),
        leg.isFlyOverFix(),
        leg.isPublishedHoldingFix()
    );
  }

  static RouteFix translate(Fix fix) {
    return new RouteFix(fix.fixIdentifier(), fix.latitude(), fix.longitude())
        .magneticVariation(fix.magneticVariation().map(MagneticVariation::angle).map(Course::inDegrees).orElse(null));
  }

  static RouteConstraint translate(Range<Double> range) {
    if (!range.hasLowerBound() && !range.hasUpperBound()) {
      return null;
    } else {
      return new RouteConstraint()
          .min(range.hasLowerBound() ? range.lowerEndpoint() : null)
          .max(range.hasUpperBound() ? range.upperEndpoint() : null);
    }
  }

  static TurnDirection translate(org.mitre.tdp.boogie.TurnDirection turnDirection) {
    if (turnDirection.isRight() && turnDirection.isLeft()) {
      return TurnDirection.BOTH;
    } else {
      return turnDirection.isLeft() ? TurnDirection.LEFT : TurnDirection.RIGHT;
    }
  }
}
