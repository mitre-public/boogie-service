package org.mitre.tdp.boogie.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("fileEventGenerator-s3OneObject")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
class S3OneObjectEventGeneratorConfigurationTest {

  @Autowired
  FileEventGenerator generator;

  @Test
  void smokeTest() {
    assertNotNull(generator);
  }
}
