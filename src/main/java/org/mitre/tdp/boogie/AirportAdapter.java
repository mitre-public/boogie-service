package org.mitre.tdp.boogie;

import java.util.List;
import java.util.function.Function;

import org.mitre.caasd.commons.Course;

final class AirportAdapter implements Function<Airport, org.mitre.caasd.app.controllers.Airport> {

    private static final RunwayAdapter RUNWAY_ADAPTER = new RunwayAdapter();

    AirportAdapter() {
    }

    @Override
    public org.mitre.caasd.app.controllers.Airport apply(Airport boogieAirport) {
        List<org.mitre.caasd.app.controllers.Runway> returnRunways = boogieAirport.runways().stream()
            .map(RUNWAY_ADAPTER)
            .toList();

        org.mitre.caasd.app.controllers.Airport returnAirport = new org.mitre.caasd.app.controllers.Airport(
            boogieAirport.airportIdentifier(),
            boogieAirport.latLong().latitude(),
            boogieAirport.latLong().longitude(),
            returnRunways
        );

        Double magVar = boogieAirport.magneticVariation().map(MagneticVariation::angle).map(Course::inDegrees).orElse(null);
        returnAirport.magneticVariation(magVar);
        
        return returnAirport;
    }
}