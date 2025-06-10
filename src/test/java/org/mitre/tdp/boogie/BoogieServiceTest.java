package org.mitre.tdp.boogie;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mitre.tdp.boogie.alg.facade.ExpandedRoute;
import org.mitre.tdp.boogie.alg.facade.RouteDetails;

class BoogieServiceTest {

  private static Boogie service;

  @BeforeAll
  static void setup() {
    service = BoogieService.boogie();

    BoogieEvent.New424File event = BoogieEvent.new424File(
        BoogieServiceTest.class,
        () -> new FileInputStream("src/test/resources/routeExpansionService/kjfk-and-friends.txt")
    );

    service.onApplicationEvent(event);
  }

  @Test
  void testDomesticRouteExpansion() {

    String sequence = service.expandRoute("KJFK.DEEZZ5.CANDR", RouteDetails.builder().build())
        .map(this::sequence)
        .orElse("");

    assertEquals("KJFK|DEEZZ|HEERO|KURNL|CANDR", sequence, "Expected fix sequence.");
  }

  @Test
  @Disabled("This test requires the airport codes mapping factory content from ttfs")
  void testInternationalRouteExpansion() {

    String sequence = service.expandRoute("KJFK..WSSS", RouteDetails.builder().build())
        .map(this::sequence)
        .orElse("");

    assertEquals("KJFK|WSSS", sequence, "Expected domestic and international airport.");
  }

  @Test
  void testDomesticAirportSearch() {
    Optional<Airport> icaoAirport = service.findAirportById("KJFK");
    assertNotNull(icaoAirport.get());
    assertEquals(icaoAirport.get().airportIdentifier(), "KJFK");

    Optional<Airport> nonIcaoAirport = service.findAirportById("47N");
    assertNotNull(nonIcaoAirport.get());
    assertEquals(nonIcaoAirport.get().airportIdentifier(), "47N");
  }

  @Test
  void testDomesticMultiAirportSearch() {
    Collection<Airport> airports = service.findAirportsById("KJFK", "KLGA", "KBOS");
    
    assertEquals(airports.size(), 3);
  }  

  private String sequence(ExpandedRoute expanded) {
    return expanded.legs().stream()
        .flatMap(l -> l.associatedFix().stream())
        .map(Fix::fixIdentifier)
        .collect(joining("|"));
  }

  @Test
  void testFirSearch() {
    Airspace airspace = service.findFir("KZAB-ZRZX").get();
    assertEquals(754, airspace.sequences().size(), "its both types");
  }

  @Test
  void testUir() {
    Airspace airspace = service.findUir("KZAB-ZRZX").get();
    assertEquals(754, airspace.sequences().size(), "Its both types");
  }

  @Test
  void getFirUirIdents() {
    Collection<String> them = service.findIdentifiersInRegion("USA");
    assertEquals(3, them.size());
  }
}
