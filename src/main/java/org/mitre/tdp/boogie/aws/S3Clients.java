package org.mitre.tdp.boogie.aws;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.S3Request;

public final class S3Clients {

  private S3Clients() {
    throw new IllegalStateException("Static factory class.");
  }

  /**
   * Shortcut to create a synchronous {@link S3Client} based on the {@link #syncBuilder(Region, AwsCredentialsPacket)} configuration.
   */
  public static S3Client sync(Region region, AwsCredentialsPacket awsCredentialsPacket) {
    return syncBuilder(region, awsCredentialsPacket).build();
  }

  /**
   * Returns a new {@link S3ClientBuilder} implementation pre-configured with region/credentialing defaults and a choice of a
   * {@link SdkHttpClient} to get out ahead of classpath conflicts.
   *
   * <p>This is mainly provided to allow access to the {@link S3ClientBuilder#overrideConfiguration(ClientOverrideConfiguration)}
   * settings (and potentially share <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-configuration-apache.html">a HTTP client</a>).
   */
  public static S3ClientBuilder syncBuilder(Region region, AwsCredentialsPacket awsCredentialsPacket) {
    return S3Client.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(awsCredentialsPacket.get())
        .region(region);
  }

  /**
   * Shortcut to create an asynchronous {@link S3AsyncClient} based on the {@link #asyncBuilder(Region, AwsCredentialsPacket)}
   * configuration.
   */
  public static S3AsyncClient async(Region region, AwsCredentialsPacket awsCredentialsPacket) {
    return asyncBuilder(region, awsCredentialsPacket).build();
  }

  /**
   * Returns a new {@link S3AsyncClientBuilder} implementation pre-configured with region/credentialing defaults and a choice
   * of a {@link SdkAsyncHttpClient} to get out ahead of classpath conflicts.
   *
   * <p>This is mainly provided to allow access to the {@link S3AsyncClientBuilder#overrideConfiguration(ClientOverrideConfiguration)}
   * settings (and potentially share <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-configuration-netty.html">a HTTP client</a>).
   */
  public static S3AsyncClientBuilder asyncBuilder(Region region, AwsCredentialsPacket awsCredentialsPacket) {
    return S3AsyncClient.builder()
        .httpClientBuilder(NettyNioAsyncHttpClient.builder())
        .credentialsProvider(awsCredentialsPacket.get())
        .region(region);
  }

  /**
   * Wraps a configured {@link S3AsyncClient} in a builder which can be used to chain on additional functionality provided
   * by this repo such as rate-limiting and retry strategies.
   */
  public static Builder wrap(S3AsyncClient client) {
    return new Builder(client);
  }

  /**
   * The default rate limits for the various request types supported by the decorated client come directly from the latest AWS
   * client API documentation: <a href="https://docs.aws.amazon.com/streams/latest/dev/service-sizes-and-limits.html">limits</a>
   */
  private static Map<Class<? extends S3Request>, Double> s3Rates(double dividedBy) {
    return Map.ofEntries();
  }

  public static final class Builder {

    private S3AsyncClient client;

    private Builder(S3AsyncClient client) {
      this.client = requireNonNull(client);
    }

    /**
     * Returns decorated version of the provided {@link S3AsyncClient} with rate limiting implemented for all submitted requests.
     *
     * <p>Without this layer client users may send enough requests to the client to exceed the request limits for certain request
     * types. When these limits are exceeded the SDK clients throw subclasses of {@link RuntimeException} which may otherwise crash
     * an unsuspecting application.
     *
     * <p>To handle this, users of SDK clients often must implement custom rate-limiting logic in their code in or around their calls
     * to the SDK methods - this class seeks to hide that complication from user-code by internally performing rate-limiting client
     * side behind the facade of a standard SDK client implementation.
     */
    public Builder standardRateLimits() {
      return customizeRateLimits(s3Rates(1.1), Map.of());
    }

    /**
     * Returns a new {@link S3AsyncClient} with the provided overrides for max-in-flight and TPS values of various request
     * types.
     *
     * <p>This is provided only for completeness - the default TPS/max in flight requests settings are pulled directly from the AWS
     * documentation and are sufficient for almost all use-cases.
     *
     * <p>Note: TPS = Transactions Per Second
     *
     * <p>See {@link #standardRateLimits()} for more details.
     */
    public Builder customizeRateLimits(
        Map<Class<? extends S3Request>, Double> tpsOverrides,
        Map<Class<? extends S3Request>, Integer> mifOverrides
    ) {
      RequestLimiters.Builder<S3Request> builder = RequestLimiters.builder();

      tpsOverrides.forEach(builder::limitRequestsPerSecondFor);
      mifOverrides.forEach(builder::limitMaxInFlightRequestsTo);

      this.client = RateLimitedS3Client.withLimiters(client, builder.build());
      return this;
    }

    public S3AsyncClient build() {
      return client;
    }
  }
}