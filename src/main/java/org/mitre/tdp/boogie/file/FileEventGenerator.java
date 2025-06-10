package org.mitre.tdp.boogie.file;

import java.io.File;
import java.io.InputStream;

import org.mitre.tdp.boogie.BoogieEvent.New424File;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mitre.tdp.boogie.BoogieEvent.new424File;

/**
 * Marker class for beans which can be added to the application context that generate {@link New424File} events.
 */
@FunctionalInterface
public interface FileEventGenerator extends ApplicationListener<ApplicationReadyEvent> {

  /**
   * Returns a generator that generates no events on application startup.
   */
  static FileEventGenerator noop() {
    return new Noop();
  }

  /**
   * Returns a generator that will generate a single event pointing to the given file on the local filesystem (handling compression)
   * and then generate no more events.
   *
   * @param publisher the publisher to use to ship the event
   * @param file      the file to wrap as a callable {@link InputStream}
   */
  static FileEventGenerator oneFs(ApplicationEventPublisher publisher, File file) {
    return ready -> publisher.publishEvent(
        new424File(
            String.format("SingleFsEventGenerator[%s]", file.getName()),
            new FileInputStreamProvider(file)
        )
    );
  }

  /**
   * Returns a generator that will generate a single event pointing to the provided file in the given bucket with the given key
   * and then generate no more events.
   *
   * @param publisher  the publisher to use to ship the event
   * @param client     the S3 client implementation to use to load the file
   * @param bucketName the name of the bucket to load the file from
   * @param fileKey    the key of the file in the bucket
   */
  static FileEventGenerator oneS3(ApplicationEventPublisher publisher, S3Client client, String bucketName, String fileKey) {
    return ready -> publisher.publishEvent(
        new424File(
            String.format("SingleS3EventGenerator[bucket=%s,key=%s]", bucketName, fileKey),
            new S3InputStreamProvider(client, bucketName, fileKey)
        )
    );
  }

  /**
   * Returns a generator that will generate a single event pointing to the provided file in the given bucket with the given key
   * and then generate no more events.
   *
   * @param publisher  the publisher to use to ship the event
   * @param client     the S3 client implementation to use to load the file
   * @param bucketName the name of the bucket to load the file from
   */
  static FileEventGenerator latestS3(ApplicationEventPublisher publisher, S3Client client, String bucketName) {
    return new LatestS3FileEventGenerator(publisher, client, bucketName);
  }

  final class Noop implements FileEventGenerator {

    private Noop() {
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
    }
  }
}
