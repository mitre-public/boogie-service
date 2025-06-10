package org.mitre.tdp.boogie;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mitre.caasd.app.controllers.*;
import org.mitre.caasd.app.controllers.AirspaceType;

class ApiConverterTest {
  private static Boogie service;
  private static final AirportAdapter AIRPORT_ADAPTER = new AirportAdapter();
  private static final AirportGeoJsonAdapter AIRPORT_GEOJSON_ADAPTER = new AirportGeoJsonAdapter();
  private static final FirUirAdapter FIR_UIR_ADAPTER = new FirUirAdapter();
  private static final AirspaceGeoJsonAdapter AIRSPACE_GEOJSON_ADAPTER = new AirspaceGeoJsonAdapter();
  private static final AirspaceIdentifierAdapter AIRSPACE_IDENTIFIER_ADAPTER = new AirspaceIdentifierAdapter();

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
  void testAirspaceIdents() {
    Collection<String> strings = service.findIdentifiersInRegion("USA");
    Collection<AirspaceIdentifier> identifiers = strings.stream().map(AIRSPACE_IDENTIFIER_ADAPTER).toList();
    assertEquals(3, identifiers.size(), "one dual and one single fir");
  }

  @Test
  void testFirUirLooksups() {
    Airspace fir = service.findFir("KZAK-ZOZX").get();
    Airspace uir = service.findUir("KZAB-ZRZX").get();

    org.mitre.caasd.app.controllers.Airspace apiFir = FIR_UIR_ADAPTER.apply(fir);
    org.mitre.caasd.app.controllers.Airspace apiUir = FIR_UIR_ADAPTER.apply(uir);

    assertAll(
        () -> assertEquals("KZAK-ZOZX", apiFir.getIdentifier()),
        () -> assertEquals(3, apiFir.getSequences().size()),
        () -> assertEquals(AirspaceType.FIR, apiFir.getAirspaceType()),
        () -> assertEquals("KZAB-ZRZX", apiUir.getIdentifier()),
        () -> assertEquals(754, apiUir.getSequences().size()),
        () -> assertEquals(18000, apiUir.getAltitudeLimit().getMin()),
        () -> assertNull(apiUir.getAltitudeLimit().getMax())
    );
  }

  @Test
  void testAirspaceGeoJson() {
    Airspace fir = service.findFir("KZAK-ZOZX").get();
    Airspace uir = service.findUir("KZAB-ZRZX").get();
    Polygon firJson = AIRSPACE_GEOJSON_ADAPTER.apply(fir);
    Polygon uirJson = AIRSPACE_GEOJSON_ADAPTER.apply(uir);
    List<Double> firFirst = firJson.getCoordinates().get(0);
    List<Double> firLast = firJson.getCoordinates().get(firJson.getCoordinates().size() - 1);
    List<Double> uirFirst = uirJson.getCoordinates().get(0);
    List<Double> uirLast = uirJson.getCoordinates().get(uirJson.getCoordinates().size() - 1);
    assertAll(
        () -> assertEquals(659, firJson.getCoordinates().size(), "Lots more of the things because of expanding the legs"),
        () -> assertEquals(925, uirJson.getCoordinates().size(), "Lots more legs because expanding the legs out"),
        () -> assertEquals(firFirst, firLast, "polygon needs to be closed"),
        () -> assertEquals(uirFirst, uirLast, "polygon needs to be closed")
    );
  }

  @Test
  void testAirportApiReturn() {
    Optional<Airport> airport = service.findAirportById("KJFK");
    assertNotNull(airport.get());
    
    org.mitre.caasd.app.controllers.Airport apiAirport = AIRPORT_ADAPTER.apply(airport.get());

    assertEquals(apiAirport.getLatitude(), 40.63992777777777);
    assertEquals(apiAirport.getLongitude(), -73.77869166666666);
    assertEquals(apiAirport.getRunways().size(), 8);
  }

  @Test
  void testAirportApiGeoJsonReturn() {
    Optional<Airport> airport = service.findAirportById("KJFK");
    assertNotNull(airport.get());
    
    Feature airportFeature = AIRPORT_GEOJSON_ADAPTER.apply(airport.get());

    // lat / lon order must be correct here ... :D
    assertEquals(airportFeature.getGeometry().getCoordinates().get(0), -73.77869166666666);
    assertEquals(airportFeature.getGeometry().getCoordinates().get(1), 40.63992777777777);
    assertEquals(airportFeature.getGeometry().getType(), Point.TypeEnum.POINT);
    assertEquals(airportFeature.getType(), Feature.TypeEnum.FEATURE);
    assertNotNull(airportFeature.getProperties());
  }
}
