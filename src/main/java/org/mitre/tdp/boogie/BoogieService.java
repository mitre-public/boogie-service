package org.mitre.tdp.boogie;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.mitre.caasd.commons.util.DemotedException;
import org.mitre.tdp.boogie.alg.facade.ExpandedRoute;
import org.mitre.tdp.boogie.alg.facade.FluentRouteExpander;
import org.mitre.tdp.boogie.alg.facade.RouteDetails;
import org.mitre.tdp.boogie.arinc.ArincVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Boogie class that does the state loading
final public class BoogieService implements Boogie {

  /**
   * Creates a new instance of a {@link BoogieService} backed by Boogie's {@link FluentRouteExpander} and sensitive to
   * the detection of new 424 files which should back it.
   */
  static Boogie boogie() {
    return new BoogieService();
  }

  // atomic reference to BoogieState
  private static final Logger LOG = LoggerFactory.getLogger(BoogieService.class);

  /**
   * Ensure the boogieState accessed by different threads (if present) aren't different (i.e. one thread gets a stale copy).
   */
  private final AtomicReference<BoogieState> boogieState;

  private BoogieService() {
    this.boogieState = new AtomicReference<>();
  }

  @Override
  public Optional<ExpandedRoute> expandRoute(String route, RouteDetails routeDetails) {
    if (boogieState.get() == null) {
      LOG.warn("expandRoute - Received query before boogie state was initialized. Request contents were, route: {}, details: {}", route, routeDetails);
      return Optional.empty();
    }
    return boogieState.get().fluentRouteExpander().expand(route, routeDetails);
  }

  @Override
  public Optional<Airport> findAirportById(String airportId) {
    if (boogieState.get() == null) {
      LOG.warn("findAirportById - Received query before boogie state was initialized. Request contents were, route: {}", airportId);
      return Optional.empty();
    }
    Optional<Airport> match = boogieState.get().airports(airportId).stream()
        .filter(airport -> airport.airportIdentifier().equals(airportId)).findFirst();

    return match;
  }

  @Override
  public Collection<Airport> findAirportsById(String... airportIds) {
    if (boogieState.get() == null) {
      LOG.warn("findAirportsById - Received query before boogie state was initialized. Request contents were, route: {}", airportIds.toString());
      return new ArrayList<>();
    }
    return boogieState.get().airports(airportIds);
  }

  // implement interfaces
  @Override
  public void onApplicationEvent(BoogieEvent.New424File event) {

    try (InputStream is = event.fileContents()) {
      // reload the boogieState using the new input stream
      if (boogieState.get() == null) {
        boogieState.set(BoogieState.forVersion(ArincVersion.V19));
      }
      boogieState.get().accept(is);
    } catch (Exception e) {
      e.printStackTrace(); // demoted exception is not thrown/logged?
      throw DemotedException.demote("Exception encountered attempting to process new incoming 424 file: " + event, e);
    }
  }

  @Override
  public Optional<Airspace> findFir(String id) {
    if (boogieState.get() == null) {
      LOG.warn("findFir - Received query before boogie state was initialized. Request contents were, route: {}", id);
      return Optional.empty();
    }
    Optional<Airspace> airspace = boogieState.get().airspaces(id).stream()
        .filter(i -> i.airspaceType().equals(AirspaceType.FIR))
        .findFirst();
    return airspace;
  }

  @Override
  public Optional<Airspace> findUir(String id) {
    if (boogieState.get() == null) {
      LOG.warn("findUir - Received query before boogie state was initialized. Request contents were, route: {}", id);
      return Optional.empty();
    }
    Optional<Airspace> airspace = boogieState.get().airspaces(id).stream()
        .filter(i -> i.airspaceType().equals(AirspaceType.UIR))
        .findFirst();
    return airspace;
  }

  @Override
  public Collection<String> findIdentifiersInRegion(String... arincRegion) {
    if (boogieState.get() == null) {
      LOG.warn("findIdentifiersInRegion - Received query before boogie state was initialized. Request contents were: ".concat(Arrays.toString(arincRegion)));
      return Collections.emptyList();
    }
    return boogieState.get().identifiersInAreaCode(arincRegion);
  }
}


// no op that implements the interfaces
final class Noop implements Boogie {

  private static final Logger LOG = LoggerFactory.getLogger(Noop.class);

  /**
   * Creates a new no-op version of a route expansion service which always returns {@link Optional#empty()}.
   */
  static Boogie noop() {
    return new Noop();
  }

  private Noop() {}

  @Override
  public Optional<ExpandedRoute> expandRoute(String route, RouteDetails routeDetails) {
    LOG.warn("Noop Boogie - returning empty route expansion");
    return Optional.empty();
  }

  @Override
  public Optional<Airport> findAirportById(String airportId) {
    LOG.warn("Noop Boogie - returning empty airport");
    return Optional.empty();
  }

  @Override
  public Collection<Airport> findAirportsById(String... airportIds) {
    LOG.warn("Noop Boogie - returning empty airports");
    return new ArrayList<Airport>();
  }

  @Override
  public void onApplicationEvent(BoogieEvent.New424File event) {
    LOG.warn("Noop Boogie - skipping onApplicationEvent()");
  }

  @Override
  public Optional<Airspace> findFir(String id) {
    LOG.warn("Noop Boogie - returning empty FIR");
    return Optional.empty();
  }

  @Override
  public Optional<Airspace> findUir(String id) {
    LOG.warn("Noop Boogie - returning empty UIR");
    return Optional.empty();
  }

  @Override
  public Collection<String> findIdentifiersInRegion(String... arincRegion) {
    LOG.warn("Noop Boogie - returning empty identifiers");
    return List.of();
  }
}
