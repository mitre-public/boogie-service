package org.mitre.tdp.boogie;

import java.util.function.Function;

import org.mitre.caasd.commons.Course;
import org.mitre.caasd.commons.Distance;

final class RunwayAdapter implements Function<Runway, org.mitre.caasd.app.controllers.Runway> {
    RunwayAdapter() {
    }

    @Override
    public org.mitre.caasd.app.controllers.Runway apply(Runway runway) {
        org.mitre.caasd.app.controllers.Runway retRunway = new org.mitre.caasd.app.controllers.Runway(
            runway.runwayIdentifier(),
            runway.origin().latitude(),
            runway.origin().longitude()
        );

        runway.length().map(Distance::inFeet).ifPresent(retRunway::length);
        runway.course().map(Course::inDegrees).ifPresent(retRunway::course);

        return retRunway;
    }
}
