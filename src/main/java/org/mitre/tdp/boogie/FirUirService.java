package org.mitre.tdp.boogie;

import java.util.Collection;
import java.util.Optional;

public interface FirUirService {
  /**
   * This provides a collection of FIR airspace that match the id;
   * @param id The FIR Identifier '-' the FIR Address e.g., KZAB-ZRZX
   * @return the FIRs and UIR airspace matching that id.
   */
  Optional<Airspace> findFir(String id);

  /**
   * This provides a collection of airspace that match the id;
   * @param id The UIR Identifier '-' the UIR Address e.g., KZAB-ZRZX
   * @return the FIRs and UIR airspace matching that id.
   */
  Optional<Airspace> findUir(String id);

  /**
   * This returns the identifier-address of all FIRUIRs in the arinc region
   * @param arincRegion arinc region e.g., USA, PAC
   * @return a list of idents e.g., KZAB-ZRZX
   */
  Collection<String> findIdentifiersInRegion(String... arincRegion);
}
