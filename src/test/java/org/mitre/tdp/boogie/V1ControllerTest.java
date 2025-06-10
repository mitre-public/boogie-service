package org.mitre.tdp.boogie;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mitre.caasd.app.controllers.Equipage;
import org.mitre.caasd.app.controllers.RouteToExpand;
import org.mitre.tdp.boogie.alg.facade.RouteDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

class V1ControllerTest {

  @Test
  void testRouteDetails_AllOptionalsNull() {

    RouteDetails actual = V1Controller.routeDetails(new RouteToExpand("A..B"));
    RouteDetails expected = RouteDetails.builder().equipagePreference(List.of()).build();

    assertEquals(expected, actual);
  }

  @Test
  void testRouteDetails_AllOptionalsPresent() {

    RouteToExpand toExpand = new RouteToExpand("A..B")
        .arrivalRunway("AR")
        .departureRunway("DR")
        .equipagePreference(List.of(Equipage.RNAV));

    RouteDetails actual = V1Controller.routeDetails(toExpand);

    RouteDetails expected = RouteDetails.builder()
        .arrivalRunway("AR")
        .departureRunway("DR")
        .equipagePreference(RequiredNavigationEquipage.RNAV)
        .build();

    assertEquals(expected, actual);
  }
}
