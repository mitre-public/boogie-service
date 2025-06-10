package org.mitre.tdp.boogie.file;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mitre.tdp.boogie.BoogieEvent;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LatestS3FileEventGeneratorTest {

  @Test
  void testScanAndEmit() {

    ListObjectsResponse response = ListObjectsResponse.builder()
        .contents(List.of(
            S3Object.builder()
                .key("1906.gz")
                .build(),
            S3Object.builder()
                .key("1908.gz")
                .build(),
            S3Object.builder()
                .key("1907.gz")
                .build()
        ))
        .build();

    S3Client client = mock(S3Client.class);
    when(client.listObjects(any(Consumer.class))).thenReturn(response);

    CollectingPublisher publisher = new CollectingPublisher();
    LatestS3FileEventGenerator generator = new LatestS3FileEventGenerator(publisher, client, "bucket");

    generator.scanAndEmit();
    assertTrue(publisher.hasEvent(0), "An event should have been emitted.");

    BoogieEvent.New424File event = publisher.get(0);
    assertEquals(generator.formatEventSource("1908.gz"), event.getSource(), "Event source should be 1908.gz cycle.");

    generator.scanAndEmit();
    assertFalse(publisher.hasEvent(1), "No new event should be generated.");
  }

  private static final class CollectingPublisher implements ApplicationEventPublisher {

    private final List<Object> events;

    private CollectingPublisher() {
      this.events = new ArrayList<>();
    }

    @Override
    public void publishEvent(Object event) {
      events.add(event);
    }

    public boolean hasEvent(int eventNumber) {
      return eventNumber < events.size();
    }

    public <T> T get(int eventNumber) {
      return (T) events.get(eventNumber);
    }
  }
}
