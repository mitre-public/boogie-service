package org.mitre.tdp.boogie;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BoogieConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "boogieService.boogie", name = "enabled", havingValue = "true")
  Boogie boogieService() {
    return BoogieService.boogie();
  }

  @Bean
  @ConditionalOnProperty(prefix = "boogieService.noop", name = "enabled", havingValue = "true")
  Boogie noopService() {
    return Noop.noop();
  }
}
