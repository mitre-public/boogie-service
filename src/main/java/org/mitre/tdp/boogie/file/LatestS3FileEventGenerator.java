package org.mitre.tdp.boogie.file;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.mitre.tdp.boogie.AiracCycle;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;

import static java.util.Objects.requireNonNull;
import static org.mitre.tdp.boogie.BoogieEvent.new424File;

final class LatestS3FileEventGenerator implements FileEventGenerator, AutoCloseable {

  private final ApplicationEventPublisher publisher;

  private final S3Client client;

  private final String bucket;

  private final ScheduledExecutorService executorService;

  private final AtomicReference<String> lastProcessed;

  LatestS3FileEventGenerator(ApplicationEventPublisher publisher, S3Client client, String bucket) {
    this.publisher = requireNonNull(publisher);
    this.client = requireNonNull(client);
    this.bucket = requireNonNull(bucket);
    this.executorService = Executors.newScheduledThreadPool(1);
    this.lastProcessed = new AtomicReference<>();
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    executorService.scheduleAtFixedRate(this::scanAndEmit, 0, 1, TimeUnit.HOURS);
  }

  @Override
  public void close() throws Exception {
    executorService.shutdown();
  }

  void scanAndEmit() {
    latestObject().ifPresent(this::handleLatestObject);
  }

  private void handleLatestObject(String latestObject) {
    String last = lastProcessed.getAndSet(latestObject);
    if (last == null || !last.equals(latestObject)) {
      publishObject(latestObject);
    }
  }

  String formatEventSource(String object) {
    return String.format("LatestS3FileEventGenerator[bucket=%s,key=%s]", bucket, object);
  }

  private void publishObject(String object) {
    publisher.publishEvent(
        new424File(
            formatEventSource(object),
            new S3InputStreamProvider(client, bucket, object)
        )
    );
  }

  /**
   * Returns an object associated with the latest cycle of infrastructure data available
   *
   * <p>The object keys are assumed to be prefixed with the AIRAC cycle they're effective for, e.g. {@code 1901} and therefore
   * have a natural sort order we can use to find the latest.
   */
  private Optional<String> latestObject() {
    return client.listObjects(b -> b.bucket(bucket))
        .contents()
        .stream()
        .map(S3Object::key)
        .filter(k -> AiracCycle.isValidCycle(k.substring(0, 4)))
        .max(Comparator.naturalOrder());
  }
}
