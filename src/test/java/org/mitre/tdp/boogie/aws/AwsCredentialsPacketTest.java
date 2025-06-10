package org.mitre.tdp.boogie.aws;

import java.io.UncheckedIOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AwsCredentialsPacketTest {

  @AfterEach
  void clearSystemSettings() {
    System.clearProperty(SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL.property());
    System.clearProperty(SdkSystemSetting.ASYNC_HTTP_SERVICE_IMPL.property());
  }

  @Test
  void testAutoCredentialProvider() {
    AwsCredentialsProvider provider = AwsCredentialsPacket.builder().build().get();
    assertEquals(DefaultCredentialsProvider.class, provider.getClass(), "Expected default provider chain.");
  }

  @Test
  void testWebCredentialProvider() {
    AwsCredentialsProvider provider = AwsCredentialsPacket.builder().credentialsProvider("WEB").build().get();
    assertEquals(WebIdentityTokenFileCredentialsProvider.class, provider.getClass(), "Expected web-based credential resolver.");
  }

  /**
   * In order to resolve credentials with the {@link WebIdentityTokenFileCredentialsProvider} AWS requires the STS library to be
   * on the classpath. We want to ensure that this module includes that for users (at least onto their runtime classpath).
   * <p>
   * In this test (with the provided system properties) {@link WebIdentityTokenFileCredentialsProvider#resolveCredentials()} should
   * throw an exception - however it shouldn't be the {@link IllegalStateException} indicating STS is missing.
   * <p>
   * Instead, it should be a read error (as asserted below) as the web provider finishes its pre-checks at attempts to read the
   * web identity file.
   */
  @Test
  void testWebCredentialProvider_DoesntThrowSTSError() {
    System.setProperty("aws.webIdentityTokenFile", "someFile");
    System.setProperty("aws.roleArn", "someRole");

    AwsCredentialsProvider provider = AwsCredentialsPacket.builder().credentialsProvider("WEB").build().get();
    assertThrows(UncheckedIOException.class, provider::resolveCredentials, "Incorrect error encountered when attempting WEB credential resolution - see test JDOC.");
  }

  @Test
  void testContainerCredentialProvider() {
    AwsCredentialsProvider provider = AwsCredentialsPacket.builder().credentialsProvider("CONTAINER").build().get();
    assertEquals(ContainerCredentialsProvider.class, provider.getClass(), "Expected container-based credential resolver.");
  }

  @Test
  void testProfileCredentialProvider() {
    AwsCredentialsProvider provider = AwsCredentialsPacket.builder()
        .credentialsProvider("PROFILE")
        .awsProfileName("myProfile")
        .build()
        .get();

    assertAll(
        () -> assertEquals(ProfileCredentialsProvider.class, provider.getClass(), "Expected profile credential provider."),
        () -> assertThrows(NullPointerException.class, () -> AwsCredentialsPacket.builder().credentialsProvider("PROFILE").build().get(), "Expected exception when using profile with now profile name set.")
    );
  }

  @Test
  void testBasicCredentialProvider() {
    AwsCredentialsProvider provider = AwsCredentialsPacket.builder()
        .credentialsProvider("BASIC")
        .awsAccessKeyId("myAccessKey")
        .awsSecretAccessKey("mySecretKey")
        .build()
        .get();

    assertAll(
        () -> assertEquals(AwsBasicCredentials.class, provider.resolveCredentials().getClass(), "Expected basic credential provider."),
        () -> assertThrows(NullPointerException.class, () -> AwsCredentialsPacket.builder().credentialsProvider("BASIC").awsSecretAccessKey("secret").build().get(), "Expected exception when using basic without access key."),
        () -> assertThrows(NullPointerException.class, () -> AwsCredentialsPacket.builder().credentialsProvider("BASIC").awsAccessKeyId("access").build().get(), "Expected exception when using basic without secret key.")
    );
  }

  @Test
  void testSystemSettings() {

    // should set properties on build
    AwsCredentialsPacket packet = AwsCredentialsPacket.builder()
        .credentialsProvider("PROFILE")
        .awsProfileName("MY PROFILE")
        .configureSdkHttpServiceSystemSettings()
        .build();

    assertAll(
        () -> assertEquals("software.amazon.awssdk.http.apache.ApacheSdkHttpService", System.getProperty(SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL.property()), "SYNC Service"),
        () -> assertEquals("software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService", System.getProperty(SdkSystemSetting.ASYNC_HTTP_SERVICE_IMPL.property()), "ASYNC Service")
    );
  }
}