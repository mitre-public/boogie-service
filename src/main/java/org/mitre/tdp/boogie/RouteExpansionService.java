package org.mitre.tdp.boogie;

import java.util.Optional;

import org.mitre.tdp.boogie.alg.facade.ExpandedRoute;
import org.mitre.tdp.boogie.alg.facade.FluentRouteExpander;
import org.mitre.tdp.boogie.alg.facade.RouteDetails;
import org.mitre.tdp.boogie.alg.facade.RouteSummary;

/**
 * Represents a route expander in the context of this service as something which can receive expansion requests and events noting
 * the detection of a new 424 file (which should be used to back the service).
 */
public interface RouteExpansionService {
  /**
   * Generate an expanded {@link RouteSummary} from a collection of input expansion parameters.
   *
   * <p>This class is meant to be backed by a {@link FluentRouteExpander} and therefore mirrors its API, however the details of
   * how that expander is built should be delegated to the implementation.
   *
   * @param route        the route string to expand
   * @param routeDetails additional contextual details alongside the route to help with expansion
   */
  Optional<ExpandedRoute> expandRoute(String route, RouteDetails routeDetails);

}
