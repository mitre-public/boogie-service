package org.mitre.tdp.boogie;

import java.util.function.Function;

import org.mitre.caasd.app.controllers.AirspaceIdentifier;
import org.mitre.caasd.app.controllers.AirspaceType;

public final class AirspaceIdentifierAdapter implements Function<String, AirspaceIdentifier> {
  @Override
  public AirspaceIdentifier apply(String s) {
    String[] parts = s.split("-");
    return new AirspaceIdentifier(parts[0], parts[1], AirspaceType.valueOf(parts[2]));
  }
}
