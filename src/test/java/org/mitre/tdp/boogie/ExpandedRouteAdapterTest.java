package org.mitre.tdp.boogie;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;
import org.mitre.caasd.app.controllers.Equipage;
import org.mitre.caasd.app.controllers.RouteConstraint;
import org.mitre.caasd.app.controllers.RouteFix;
import org.mitre.caasd.commons.LatLong;
import org.mitre.tdp.boogie.alg.facade.RouteSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExpandedRouteAdapterTest {

  @Test
  void testTranslate_RouteSummary_Empty() {

    RouteSummary summary = RouteSummary.builder()
        .route("A..B")
        .departureAirport("DA")
        .arrivalAirport("AA")
        .build();

    org.mitre.caasd.app.controllers.RouteSummary expected = new org.mitre.caasd.app.controllers.RouteSummary("A..B")
        .departureAirport("DA")
        .arrivalAirport("AA");

    assertEquals(expected, ExpandedRouteAdapter.translate(summary));
  }

  @Test
  void testTranslate_RouteSummary_Full() {

    RouteSummary summary = RouteSummary.builder()
        .route("A..B")
        .departureAirport("DA")
        .departureRunway("DR")
        .departureFix("DF")
        .sid("SID")
        .sidExitFix("SIDEF")
        .requiredSidEquipage(RequiredNavigationEquipage.RNAV)
        .arrivalAirport("AA")
        .arrivalRunway("AR")
        .arrivalFix("AF")
        .star("STAR")
        .starEntryFix("STAREF")
        .requiredStarEquipage(RequiredNavigationEquipage.RNP)
        .approach("AP")
        .approachEntryFix("APEF")
        .requiredApproachEquipage(RequiredNavigationEquipage.RNP)
        .build();

    org.mitre.caasd.app.controllers.RouteSummary expected = new org.mitre.caasd.app.controllers.RouteSummary("A..B")
        .departureAirport("DA")
        .departureRunway("DR")
        .departureFix("DF")
        .sid("SID")
        .sidExitFix("SIDEF")
        .requiredSidEquipage(Equipage.RNAV)
        .arrivalAirport("AA")
        .arrivalRunway("AR")
        .arrivalFix("AF")
        .star("STAR")
        .starEntryFix("STAREF")
        .requiredStarEquipage(Equipage.RNP)
        .approach("AP")
        .approachEntryFix("APEF")
        .requiredApproachEquipage(Equipage.RNP);

    assertEquals(expected, ExpandedRouteAdapter.translate(summary));
  }

  @Test
  void testTranslate_Fix_Empty() {

    Fix fix = Fix.builder()
        .fixIdentifier("F")
        .latLong(LatLong.of(0., 0.))
        .build();

    RouteFix expected = new RouteFix("F", 0., 0.);

    assertEquals(expected, ExpandedRouteAdapter.translate(fix));
  }

  @Test
  void testTranslate_Fix_Full() {

    Fix fix = Fix.builder()
        .fixIdentifier("F")
        .latLong(LatLong.of(0., 0.))
        .magneticVariation(MagneticVariation.ZERO)
        .build();

    RouteFix expected = new RouteFix("F", 0., 0.)
        .magneticVariation(0.);

    assertEquals(expected, ExpandedRouteAdapter.translate(fix));
  }

  @Test
  void testTranslate_RouteConstraint_Empty() {
    assertNull(ExpandedRouteAdapter.translate(Range.all()));
  }

  @Test
  void testTranslate_RouteConstraint_Lower() {
    RouteConstraint expected = new RouteConstraint().min(10_000.);
    assertEquals(expected, ExpandedRouteAdapter.translate(Range.atLeast(10_000.)));
  }

  @Test
  void testTranslate_RouteConstraint_Higher() {
    RouteConstraint expected = new RouteConstraint().max(10_000.);
    assertEquals(expected, ExpandedRouteAdapter.translate(Range.atMost(10_000.)));
  }

  @Test
  void testTranslate_RouteConstraint_Full() {

    RouteConstraint expected = new RouteConstraint()
        .min(5_000.)
        .max(10_000.);

    assertEquals(expected, ExpandedRouteAdapter.translate(Range.closed(5_000., 10_000.)));
  }
}
