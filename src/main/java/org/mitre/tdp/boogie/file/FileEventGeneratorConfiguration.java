package org.mitre.tdp.boogie.file;

import java.io.File;

import org.mitre.tdp.boogie.aws.S3Clients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
class FileEventGeneratorConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "fileEventGenerator.noop", name = "enabled", havingValue = "true")
  FileEventGenerator noopFileEventGenerator() {
    return FileEventGenerator.noop();
  }

  @Bean
  @ConditionalOnProperty(prefix = "fileEventGenerator.fs.oneFile", name = "name")
  FileEventGenerator oneFsFileEventGenerator(
      ApplicationEventPublisher publisher,
      @Value("${fileEventGenerator.fs.oneFile.name}")
      File file) {
    return FileEventGenerator.oneFs(publisher, file);
  }

  @Bean
  @ConditionalOnProperty(prefix = "fileEventGenerator.s3.oneObject", name = "bucket")
  FileEventGenerator oneS3FileEventGenerator(
      ApplicationEventPublisher publisher,
      S3Client client,
      @Value("${fileEventGenerator.s3.oneObject.bucket}")
      String bucket,
      @Value("${fileEventGenerator.s3.oneObject.name}")
      String oneObject) {
    return FileEventGenerator.oneS3(publisher, client, bucket, oneObject);
  }

  @Bean
  @ConditionalOnProperty(prefix = "fileEventGenerator.s3.latestObject", name = "bucket")
  FileEventGenerator latestS3FileEventGenerator(
      ApplicationEventPublisher publisher,
      S3Client client,
      @Value("${fileEventGenerator.s3.latestObject.bucket}")
      String bucket) {
    return FileEventGenerator.latestS3(publisher, client, bucket);
  }

  @Bean
  @ConditionalOnBean(S3Properties.class)
  S3Client s3Client(S3Properties properties) {
    return S3Clients.sync(properties.region(), properties.awsCredentialsPacket());
  }
}
