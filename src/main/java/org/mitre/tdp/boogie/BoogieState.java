package org.mitre.tdp.boogie;

import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.mitre.tdp.boogie.alg.RouteExpander;
import org.mitre.tdp.boogie.alg.facade.FluentRouteExpander;
import org.mitre.tdp.boogie.arinc.ArincVersion;
import org.mitre.tdp.boogie.arinc.OneshotRecordParser;
import org.mitre.tdp.boogie.arinc.OneshotRecordParser.ClientRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Class representing the current cached state of the Boogie REST endpoint.
 * <p>
 * This singleton class (as managed by Spring) contains the single copy of all the relevant Boogie databases which are generated
 * and updated when a new ARINC-424 file is provided to the application.
 * <p>
 * If this class grows too large we'll need to consider ways to break it up into more modular chunks...
 */
final class BoogieState implements Consumer<InputStream> {

  private static final Logger LOG = LoggerFactory.getLogger(BoogieState.class);

  /**
   * The {@link ArincVersion} to use to parse the data specify the target model classes to convert the input raw records into.
   */
  private final ArincVersion arincVersion;

  /**
   * Mapping from {{@link Fix#fixIdentifier()}, {@link Fix}}.
   */
  private Multimap<String, Fix> assembledFixes;

  /**
   * Mapping from {{@link Airport#airportIdentifier()}, {@link Airport}}.
   */
  private Multimap<String, Airport> assembledAirports;

  /**
   * Mapping from {{@link Airway#airwayIdentifier()}, {@link Airway}}.
   */
  private Multimap<String, Airway> assembledAirways;

  /**
   * Mapping from {{@link Procedure#procedureIdentifier()}, {{@link Procedure#airportIdentifier()}, {@link Procedure}}.
   */
  private Multimap<String, Procedure> assembledProcedures;

  /**
   * Mapping from the FIR identifier to the "identifier-address"
   */
  private Multimap<String, Airspace> assembledFirUirs;

  /**
   * Shared {@link FluentRouteExpander} instance for general use.
   */
  private FluentRouteExpander fluentRouteExpander;

  private BoogieState(ArincVersion version) {
    this.arincVersion = requireNonNull(version);
  }

  /**
   * Creates a new {@link BoogieState}
   */
  public static BoogieState forVersion(ArincVersion version) {
    LOG.info("Instantiating new Boogie record cache for version: {}", version);
    return new BoogieState(version);
  }

  public Collection<Fix> fixes(String... identifiers) {
    return FormattingArincQuerier.streamUnique(identifiers).flatMap(identifier -> assembledFixes.get(identifier).stream()).collect(Collectors.toList());
  }

  public Collection<Airport> airports(String... airports) {
    return FormattingArincQuerier.streamUnique(airports).flatMap(airport -> assembledAirports.get(airport).stream()).collect(Collectors.toList());
  }

  public Collection<Airway> airways(String... airways) {
    return FormattingArincQuerier.streamUnique(airways).flatMap(airway -> assembledAirways.get(airway).stream()).collect(Collectors.toList());
  }

  public Collection<Procedure> procedures(String... procedures) {
    return FormattingArincQuerier.streamUnique(procedures).flatMap(procedure -> assembledProcedures.get(procedure).stream()).collect(Collectors.toList());
  }

  public Collection<Procedure> proceduresAt(String airport) {
    return FormattingArincQuerier.streamUnique(airport).flatMap(arpt -> assembledProcedures.values().stream().filter(i -> i.airportIdentifier().equals(arpt))).collect(Collectors.toList());
  }

  public FluentRouteExpander fluentRouteExpander() {
    return requireNonNull(fluentRouteExpander);
  }

  public Collection<Airspace> airspaces(String... airspaces) {
    return FormattingArincQuerier.streamUnique(airspaces).flatMap(airsapce -> assembledFirUirs.get(airsapce).stream()).collect(Collectors.toList());
  }

  public Collection<String> identifiersInAreaCode(String... areaCode) {
    return FormattingArincQuerier.streamUnique(areaCode)
        .flatMap(area -> assembledFirUirs.values().stream()
            .filter(i -> i.area().equals(area))
            .map(i -> i.identifier().concat("-").concat(i.airspaceType().name())))
        .toList();
  }

  /**
   * This relatively long (re-)initialization block is a one-shot from the raw ARINC 424 file to both the parsed records in their
   * close-to-424 format as well as thinner assembled versions of them more useful in tools like the {@link RouteExpander}.
   */
  @Override
  public void accept(InputStream is) {

    LOG.info("Accepted inputStream of 424 data - rebuilding local cache.");

    ClientRecords<Airport, Fix, Airway, Procedure, Airspace> clientRecords = OneshotRecordParser.standard(arincVersion).assembleFrom(is);

    LOG.info("Finished parsing and converting records.");

    this.assembledFixes = toMultimap(Fix::fixIdentifier, clientRecords.fixes().stream());

    LOG.info("Finished assembling {} total fixes.", this.assembledFixes.size());

    this.assembledAirports = toMultimap(Airport::airportIdentifier, clientRecords.airports().stream());

    LOG.info("Finished assembling {} total airports.", this.assembledAirports.size());

    this.assembledAirways = toMultimap(Airway::airwayIdentifier, clientRecords.airways().stream());

    LOG.info("Finished assembling {} total airways.", this.assembledAirways.size());

    this.assembledProcedures = toMultimap(Procedure::procedureIdentifier, clientRecords.procedures().stream());

    LOG.info("Finished assembling {} total procedures.", this.assembledProcedures.size());

    this.assembledFirUirs = toMultimap(Airspace::identifier, clientRecords.firUirs().stream());

    LOG.info("Finished assembling, {} total airspaces", this.assembledFirUirs.size());

    this.fluentRouteExpander = FluentRouteExpander.inMemoryBuilder(
        this.assembledAirports.values(),
        this.assembledProcedures.values(),
        this.assembledAirways.values(),
        this.assembledFixes.values()
    )
    .build();

    LOG.info("Finished construction of FluentRouteExpander.");
    LOG.info("Completed processing and re-indexing of internal cache with data from the provided input stream");
  }

  private <K, V> Multimap<K, V> toMultimap(Function<V, K> keyFn, Stream<V> stream) {
    Multimap<K, V> multimap = LinkedHashMultimap.create();
    stream.forEach(value -> multimap.put(keyFn.apply(value), value));
    return multimap;
  }
}