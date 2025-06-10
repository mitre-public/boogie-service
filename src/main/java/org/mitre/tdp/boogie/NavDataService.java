package org.mitre.tdp.boogie;

import java.util.Collection;
import java.util.Optional;

public interface NavDataService {

  /**
   * Search for an {@link Airport} record by airport identifier.
   *
   * @param airportId        The airport id to search for
   * @return                 an Optional, set if the airport was found
   */
  Optional<Airport> findAirportById(String airportId);

  /**
   * Search for multiple {@link Airport} records by airport identifier
   * 
   * @param airportIds  The airport ids to search for
   * @return            a collection (possibly empty) with the matching airport information
   */
  Collection<Airport> findAirportsById(String... airportIds);
}
