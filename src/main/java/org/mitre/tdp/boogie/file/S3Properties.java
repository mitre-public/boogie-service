package org.mitre.tdp.boogie.file;

import javax.annotation.Nullable;

import org.mitre.tdp.boogie.aws.AwsCredentialsPacket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
@ConditionalOnProperty(prefix = "fileEventGenerator.s3", name = "region")
class S3Properties {

  @Value("${fileEventGenerator.s3.credentials.provider}")
  private String provider;

  @Value("${fileEventGenerator.s3.region}")
  private String region;

  @Value("${fileEventGenerator.s3.credentials.profile:#{null}}")
  private @Nullable String profile;

  @Value("${fileEventGenerator.s3.credentials.accessKey:#{null}}")
  private @Nullable String accessKey;

  @Value("${fileEventGenerator.s3.credentials.secretKey:#{null}}")
  private @Nullable String secretKey;

  public AwsCredentialsPacket awsCredentialsPacket() {
    return AwsCredentialsPacket.builder()
        .credentialsProvider(provider)
        .awsProfileName(profile)
        .awsAccessKeyId(accessKey)
        .awsSecretAccessKey(secretKey)
        .build();
  }

  public Region region() {
    return Region.of(region);
  }
}
