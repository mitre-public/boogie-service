package org.mitre.tdp.boogie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("routeExpansionService-boogie")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
class BoogieServiceConfigurationTest {

  @Autowired
  private Boogie service;

  @Test
  void smokeTest() {
    assertNotNull(service, "Service implementation should be wireable.");
  }
}
