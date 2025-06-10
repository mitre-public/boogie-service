package org.mitre.tdp.boogie;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.mitre.caasd.app.controllers.*;
import org.mitre.caasd.app.controllers.Airport;
import org.mitre.caasd.app.controllers.Airspace;
import org.mitre.tdp.boogie.alg.facade.RouteDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@RestController
class V1Controller implements V1Api {

  private static final ExpandedRouteAdapter EXPANDED_ROUTE_ADAPTER = new ExpandedRouteAdapter();
  private static final AirportAdapter AIRPORT_ADAPTER = new AirportAdapter();
  private static final AirportGeoJsonAdapter AIRPORT_GEOJSON_ADAPTER = new AirportGeoJsonAdapter();
  private static final FirUirAdapter FIR_UIR_ADAPTER = new FirUirAdapter();
  private static final AirspaceGeoJsonAdapter AIRSPACE_GEO_JSON_ADAPTER = new AirspaceGeoJsonAdapter();
  private static final AirspaceIdentifierAdapter AIRSPACE_IDENTIFIER_ADAPTER = new AirspaceIdentifierAdapter();

  private final Boogie service;

  V1Controller(Boogie service) {
    this.service = requireNonNull(service);
  }

  @Override
  public Mono<ResponseEntity<RouteResults>> expand(Mono<RouteSet> body, final ServerWebExchange exchange) {
    return body.map(rs -> ResponseEntity.ok(expandRoutes(rs)));
  }

  private RouteResults expandRoutes(RouteSet routeSet) {

    List<RouteResult> expanded = ofNullable(routeSet.getRoutes())
        .stream()
        .flatMap(Collection::stream)
        .map(rte -> new RouteResult(rte).expandedRoute(expandRoute(rte).orElse(null)))
        .toList();

    return new RouteResults(expanded);
  }

  private Optional<ExpandedRoute> expandRoute(RouteToExpand routeToExpand) {
    return service.expandRoute(routeToExpand.getRoute(), routeDetails(routeToExpand)).map(EXPANDED_ROUTE_ADAPTER);
  }

  static RouteDetails routeDetails(RouteToExpand routeToExpand) {

    List<RequiredNavigationEquipage> equipPreference = ofNullable(routeToExpand.getEquipagePreference()).stream()
        .flatMap(Collection::stream)
        .map(e -> RequiredNavigationEquipage.valueOf(e.name()))
        .toList();

    return RouteDetails.builder()
        .departureRunway(routeToExpand.getDepartureRunway())
        .arrivalRunway(routeToExpand.getArrivalRunway())
        .equipagePreference(equipPreference)
        .build();
  }
  
  @Override
  public Mono<ResponseEntity<Airport>> airportLookup(String code, final ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airport> boogieAirport = service.findAirportById(code);

    return boogieAirport
      .map(AIRPORT_ADAPTER)
      .map(apt -> Mono.just(ResponseEntity.ok(apt)))
      .orElse(Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public Mono<ResponseEntity<Feature>> airportLookupGeoJson(String code, final ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airport> boogieAirport = service.findAirportById(code);

    return boogieAirport
      .map(AIRPORT_GEOJSON_ADAPTER)
      .map(apt -> Mono.just(ResponseEntity.ok(apt)))
      .orElse(Mono.just(ResponseEntity.notFound().build())); 
  }

  @Override
  public Mono<ResponseEntity<Flux<Airport>>> airportsLookup(String codes, final ServerWebExchange exchange) {
    Collection<org.mitre.tdp.boogie.Airport> boogieAirports = service.findAirportsById(codes.split(",", -1));
    Collection<Airport> responseAirports = boogieAirports.stream().map(AIRPORT_ADAPTER).toList();
    
    Flux<Airport> fAirports = Flux.fromIterable(responseAirports);
    return Mono.just(ResponseEntity.ok(fAirports));
  }

  @Override
  public Mono<ResponseEntity<FeatureCollection>> airportsLookupGeoJson(String codes, final ServerWebExchange exchange) {
    Collection<org.mitre.tdp.boogie.Airport> boogieAirports = service.findAirportsById(codes.split(",", -1));
    
    List<Feature> responseAirportFeatures = boogieAirports.stream().map(AIRPORT_GEOJSON_ADAPTER).toList();
    
    FeatureCollection returnCollection = new FeatureCollection(FeatureCollection.TypeEnum.FEATURE_COLLECTION, responseAirportFeatures);
    
    return Mono.just(ResponseEntity.ok(returnCollection));
  }

  @Override
  public Mono<ResponseEntity<Airspace>> firLookup(String identifier, ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airspace> boogieAirspace = service.findFir(identifier);
    return boogieAirspace
        .map(FIR_UIR_ADAPTER)
        .map(air -> Mono.just(ResponseEntity.ok(air)))
        .orElse(Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public Mono<ResponseEntity<Airspace>> uirLookup(String identifier, ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airspace> boogieAirspace = service.findUir(identifier);
    return boogieAirspace
        .map(FIR_UIR_ADAPTER)
        .map(air -> Mono.just(ResponseEntity.ok(air)))
        .orElse(Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public Mono<ResponseEntity<Polygon>> uirLookupGeoJson(String identifier, ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airspace> boogieAirspace = service.findUir(identifier);
    return boogieAirspace
        .map(AIRSPACE_GEO_JSON_ADAPTER)
        .map(air -> Mono.just(ResponseEntity.ok(air)))
        .orElse(Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public Mono<ResponseEntity<Polygon>> firLookupGeoJson(String identifier, ServerWebExchange exchange) {
    Optional<org.mitre.tdp.boogie.Airspace> boogieAirspace = service.findFir(identifier);
    return boogieAirspace
        .map(AIRSPACE_GEO_JSON_ADAPTER)
        .map(air -> Mono.just(ResponseEntity.ok(air)))
        .orElse(Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public Mono<ResponseEntity<Flux<AirspaceIdentifier>>> firUirIdentifierLookup(String areaCode, ServerWebExchange exchange) {
    Collection<String> boogieIdents = service.findIdentifiersInRegion(areaCode);
    Collection<AirspaceIdentifier> idents = boogieIdents.stream().map(AIRSPACE_IDENTIFIER_ADAPTER).toList();
    Flux<AirspaceIdentifier> rIdents = Flux.fromIterable(idents);
    return Mono.just(ResponseEntity.ok(rIdents));
  }
}
