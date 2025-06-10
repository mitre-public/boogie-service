package org.mitre.tdp.boogie;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mitre.tdp.boogie.arinc.database.ArincFixDatabase;
import org.mitre.tdp.boogie.arinc.database.ArincTerminalAreaDatabase;
import org.mitre.tdp.boogie.arinc.model.ArincAirport;
import org.mitre.tdp.boogie.arinc.model.ArincLocalizerGlideSlope;
import org.mitre.tdp.boogie.arinc.model.ArincNdbNavaid;
import org.mitre.tdp.boogie.arinc.model.ArincProcedureLeg;
import org.mitre.tdp.boogie.arinc.model.ArincRunway;
import org.mitre.tdp.boogie.arinc.model.ArincVhfNavaid;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Wrapper class around a {@link ArincFixDatabase} and a {@link ArincTerminalAreaDatabase} both of which contain ARINC-424 information
 * which handles two additional cases:
 * <ul>
 *   <li>Multi-input queries</li>
 *   <li>Map-based return formatting for results</li>
 * </ul>
 */
final class FormattingArincQuerier {

  private final ArincFixDatabase fixDatabase;
  private final ArincTerminalAreaDatabase terminalAreaDatabase;

  private FormattingArincQuerier(
      ArincFixDatabase fixDatabase,
      ArincTerminalAreaDatabase terminalAreaDatabase
  ) {
    this.fixDatabase = requireNonNull(fixDatabase);
    this.terminalAreaDatabase = requireNonNull(terminalAreaDatabase);
  }

  public static FormattingArincQuerier with(ArincFixDatabase fixDatabase, ArincTerminalAreaDatabase terminalAreaDatabase) {
    return new FormattingArincQuerier(fixDatabase, terminalAreaDatabase);
  }

  public Map<String, ArincAirport> arincAirports(String... airports) {
    return streamUnique(airports)
        .map(terminalAreaDatabase::airport)
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toMap(ArincAirport::airportIdentifier, identity()));
  }

  public Optional<ArincAirport> arincAirport(String airport, String region) {
    return terminalAreaDatabase.airport(airport, region);

  }

  public Map<String, Map<String, ArincRunway>> arincRunways(String... airports) {
    return streamUnique(airports)
        .map(airport -> terminalAreaDatabase.runwaysAt(airport).stream().collect(toMap(ArincRunway::runwayIdentifier, identity())))
        .filter(map -> !map.isEmpty())
        .collect(toMap(map -> map.values().iterator().next().airportIdentifier(), identity()));
  }

  public Collection<ArincRunway> arincRunwaysAt(String airportIdent, String airportRegion) {
    return terminalAreaDatabase.runwaysAt(airportIdent, airportRegion);
  }

  public Map<String, Map<String, ArincLocalizerGlideSlope>> arincLocalizers(String... airports) {
    return streamUnique(airports)
        .map(terminalAreaDatabase::localizerGlideSlopesAt)
        .filter(map -> !map.isEmpty())
        .collect(toMap(map -> map.values().iterator().next().airportIdentifier(), identity()));
  }

  public Map<String, ArincLocalizerGlideSlope> arincLocalizer(String airportIdent, String airportRegion) {
    return terminalAreaDatabase.localizerGlideSlopesAt(airportIdent);
  }

  public Map<String, Map<String, ArincVhfNavaid>> arincVhfNavaids(String... navaids) {
    return streamUnique(navaids)
        .map(identifier -> fixDatabase.vhfNavaids(identifier).stream()
            .collect(toMap(ArincVhfNavaid::vhfIcaoRegion, identity())))
        .filter(map -> !map.isEmpty())
        .collect(toMap(map -> map.values().iterator().next().vhfIdentifier(), identity()));
  }

  public Optional<ArincVhfNavaid> arincVhfNavaid(String identifier, String region) {
    return fixDatabase.vhfNavaid(identifier, region);
  }

  public Map<String, Map<String, ArincNdbNavaid>> arincNdbNavaids(String... navaids) {
    return streamUnique(navaids)
        .map(identifier -> fixDatabase.ndbNavaids(identifier).stream()
            .collect(toMap(ArincNdbNavaid::ndbIcaoRegion, identity())))
        .filter(map -> !map.isEmpty())
        .collect(toMap(map -> map.values().iterator().next().ndbIdentifier(), identity()));
  }

  public Map<String, Collection<ArincProcedureLeg>> arincProcedureLegs(String airport, String... procedures) {
    return streamUnique(procedures)
        .collect(toMap(Function.identity(), procedure -> terminalAreaDatabase.legsForProcedure(airport.trim().toUpperCase(), procedure)));
  }

  public Collection<ArincProcedureLeg> arincProcedureLegs(String airportIdent, String airportRegion) {
    return terminalAreaDatabase.allProcedureLegsAt(airportIdent, airportRegion);
  }

  static Stream<String> streamUnique(String... names) {
    return Arrays.stream(names).map(String::trim).map(String::toUpperCase).distinct();
  }
}