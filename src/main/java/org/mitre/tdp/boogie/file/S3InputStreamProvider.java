package org.mitre.tdp.boogie.file;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;

import static java.util.Objects.requireNonNull;

final class S3InputStreamProvider implements Callable<InputStream> {

  private final S3Client client;

  private final String bucket;

  private final String objectKey;

  S3InputStreamProvider(S3Client client, String bucket, String objectKey) {
    this.client = requireNonNull(client);
    this.bucket = requireNonNull(bucket);
    this.objectKey = requireNonNull(objectKey);
  }

  @Override
  public InputStream call() throws Exception {
    InputStream raw = client.getObject(b -> b.bucket(bucket).key(objectKey), ResponseTransformer.toInputStream());
    if (objectKey.endsWith(".gz")) {
      return new GZIPInputStream(raw);
    } else if (objectKey.endsWith(".zip")) {
      ZipInputStream zin = new ZipInputStream(raw);
      ZipEntry entry;
      while ((entry = zin.getNextEntry()) != null) {
        if (entry.getName().equals("FAACIFP18")){
          return zin;
        }
      }
    }
    return raw;
  }
}
