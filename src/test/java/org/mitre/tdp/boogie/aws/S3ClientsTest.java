package org.mitre.tdp.boogie.aws;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class S3ClientsTest {

  private static final AwsCredentialsPacket PACKET = AwsCredentialsPacket.builder()
      .credentialsProvider("PROFILE")
      .awsProfileName("DUMMY")
      .build();

  private static final Region REGION = Region.US_GOV_WEST_1;

  private static final NettyNioAsyncHttpClient.Builder HTTP_CLIENT = NettyNioAsyncHttpClient.builder()
      .connectionTimeout(Duration.ofMinutes(1));

  @Test
  void smokeTest_Async() {
    assertDoesNotThrow(() -> S3Clients.async(REGION, PACKET));
  }

  @Test
  void smokeTest_AsyncBuilder() {
    assertAll(
        () -> assertDoesNotThrow(() -> S3Clients.asyncBuilder(REGION, PACKET).build(), "Normal Build"),
        () -> assertDoesNotThrow(() -> S3Clients.asyncBuilder(REGION, PACKET).httpClientBuilder(HTTP_CLIENT).build(), "HTTP Client Override")
    );
  }
}