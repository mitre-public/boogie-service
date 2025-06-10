package org.mitre.tdp.boogie.aws;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.SdkHttpService;

import static java.lang.System.setProperty;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * The {@link AwsCredentialsPacket} allows users to specific explicit {@link AwsCredentialsProvider} instances via a builder
 * typically populated from external configuration files. Usages of this class to provision credentials is preferred over the
 * standard {@link DefaultCredentialsProvider} due to idiosyncrasies in its implementation:
 * <ul>
 *   <li>It doesnt allow setting a non-default profile as the target of the chained profile-based credential provisioner</li>
 *   <li>It swallows the exceptions from credential lookup failures between providers in the chain - this can hide specific
 *   details in those exceptions that can help with debugging when the chained provider you expected to work fails</li>
 * </ul>
 * As such it's typically recommended to use the specific {@link AwsCredentialsProvider} the application is expecting to work
 * when standing up a service.
 *
 * <p>Even so the default provider this class uses is still the {@link DefaultCredentialsProvider} when no specific provider is
 * specified in the builder. However, there are a few alternates that can additionally be used via this class:
 * <ul>
 *   <li><b>BASIC</b> ({@link AwsBasicCredentials}) - which requires providing an [accessKeyId] and [awsSecretAccessKey].</li>
 *   <li><b>PROFILE</b> ({@link ProfileCredentialsProvider}) - which requires a [profileName] which exists in a visible AWS credentials
 *   file.</li>
 *   <li><b>WEB</b> ({@link WebIdentityTokenFileCredentialsProvider}) - which is typically used in EKS with OIDC to inject credentials
 *   into a container via a service account.</li>
 *   <li><b>CONTAINER</b> ({@link ContainerCredentialsProvider}) - which is used with ECS where there are native hooks into containers
 *   which can be used to get direct access to credentials closer to the account level.</li>
 * </ul>
 * Of the above four the first two are useful for local testing with session tokens setup - the latter for tracking down specific
 * errors in remote deployments.
 *
 * <p>Note: access to the underlying fields that make up the packet is provided solely to allow coercion of this model into a
 * 'credentials provider' regardless of the underlying SDK implementation (though a SDK1 conversion is not baked in).
 *
 * <p>Baked in is the SDK2 conversion (via {@link #get()}), but this should be usable as a consistent 'credentials in' structure
 * across SDK1/SDK2 API classes (with some massaging).
 */
public final class AwsCredentialsPacket implements Supplier<AwsCredentialsProvider> {

  private static final Logger LOG = LoggerFactory.getLogger(AwsCredentialsPacket.class);

  private final String credentialsProvider;
  private final String awsProfileName;
  private final String awsAccessKeyId;
  private final String awsSecretAccessKey;

  private AwsCredentialsPacket(Builder builder) {
    this.credentialsProvider = ofNullable(builder.credentialsProvider).orElse("AUTO");
    this.awsProfileName = builder.awsProfileName;
    this.awsAccessKeyId = builder.awsAccessKeyId;
    this.awsSecretAccessKey = builder.awsSecretAccessKey;
  }

  public static AwsCredentialsPacket auto() {
    return AwsCredentialsPacket.builder().credentialsProvider("AUTO").build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public String credentialsProvider() {
    return credentialsProvider;
  }

  public Optional<String> awsProfileName() {
    return ofNullable(awsProfileName);
  }

  public Optional<String> awsAccessKeyId() {
    return ofNullable(awsAccessKeyId);
  }

  public Optional<String> awsSecretAccessKey() {
    return ofNullable(awsSecretAccessKey);
  }

  /**
   * Creates a {@link AwsCredentialsProvider} based on the information configured in the builder.
   *
   * <p>For handing profiles (e.g. for MFA) if the file is in a custom location outside ~/.aws then this class can be overridden
   * via setting the ENV variable 'AWS_CREDENTIALS_PROFILES_FILE`.
   *
   * <p>Note: to use the {@code WEB} provider this class overrides the {@link SdkSystemSetting#SYNC_HTTP_SERVICE_IMPL} property
   */
  private AwsCredentialsProvider createCredentialsProvider(AwsCredentialsPacket packet) {
    switch (this.credentialsProvider) {
      case "AUTO":
        return DefaultCredentialsProvider.create();
      case "WEB":
        warnIfMissing(credentialsProvider, SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL);
        return WebIdentityTokenFileCredentialsProvider.builder().build();
      case "CONTAINER":
        warnIfMissing(credentialsProvider, SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL);
        return ContainerCredentialsProvider.builder().build();
      case "PROFILE":
        requireNonNull(packet.awsProfileName, "Profile name cannot be null if using profile credentials");
        return ProfileCredentialsProvider.create(packet.awsProfileName);
      case "BASIC":
        requireNonNull(packet.awsAccessKeyId, "Access key must be provided.");
        requireNonNull(packet.awsSecretAccessKey, "Secret key must be provided.");
        return () -> AwsBasicCredentials.create(packet.awsAccessKeyId, packet.awsSecretAccessKey);
      default:
        throw new IllegalArgumentException("Unable to create AWS credentials from Builder.");
    }
  }

  @Override
  public AwsCredentialsProvider get() {
    return createCredentialsProvider(this);
  }

  private void warnIfMissing(String providerMode, SdkSystemSetting setting) {
    if (System.getProperty(setting.property()) == null) {
      LOG.warn("Missing potentially required SDK system setting {}, in provider mode {}.", setting.property(), providerMode);
    }
  }

  public static final class Builder {

    private String credentialsProvider;
    private String awsProfileName;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private boolean configureSdkHttpServiceSystemSettings;

    /**
     * Options: AUTO, BASIC, PROFILE, WEB, CONTAINER
     *
     * <p>Default: AUTO
     */
    public Builder credentialsProvider(String credentialsProvider) {
      this.credentialsProvider = requireNonNull(credentialsProvider);
      return this;
    }

    /**
     * Required for credentialProvider=PROFILE
     */
    public Builder awsProfileName(String awsProfileName) {
      this.awsProfileName = awsProfileName;
      return this;
    }

    /**
     * Required for credentialProvider=BASIC
     */
    public Builder awsAccessKeyId(String awsAccessKeyId) {
      this.awsAccessKeyId = awsAccessKeyId;
      return this;
    }

    /**
     * Required for credentialProvider=BASIC
     */
    public Builder awsSecretAccessKey(String awsSecretAccessKey) {
      this.awsSecretAccessKey = awsSecretAccessKey;
      return this;
    }

    /**
     * <p>Enables, on {@link Builder#build()}, this to class exports {@link SdkSystemSetting}s overrides for the SYNC and ASYNC
     * HTTP <em>{@link SdkHttpService}</em> implementations used by some collection of core AWS SDK classes.
     *
     * <p>This is done here, hopefully once, to prevent runtime crop-up of the "multiple HTTP service implementations found on the
     * classpath" exception when running applications and to provide an easy way to do this as opposed to adding exclusions.
     *
     * <p>This is notable here as in the bowels of (some) {@link AwsCredentialsProvider} implementations (specifically the web one),
     * the provider pulls a client from the classpath (and there is no way to force it to use a specific one without these setings).
     *
     * <p>This is added here because:
     * <ol>
     *   <li>We're using this class everywhere to manage credentialing, and you're most likely to see this when using the WEB-based
     *   AWS credential provider.</li>
     *   <li>We don't expect clients to have strong opinions about which HTTP service is used and we don't really want them to have
     *   to dig around to find the right one, and so provide a convenience override here.</li>
     * </ol>
     */
    public Builder configureSdkHttpServiceSystemSettings() {
      this.configureSdkHttpServiceSystemSettings = true;
      return this;
    }

    public AwsCredentialsPacket build() {
      if (configureSdkHttpServiceSystemSettings) {
        setProperty(SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL.property(), "software.amazon.awssdk.http.apache.ApacheSdkHttpService");
        setProperty(SdkSystemSetting.ASYNC_HTTP_SERVICE_IMPL.property(), "software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService");
      }
      return new AwsCredentialsPacket(this.configureSdkHttpServiceSystemSettings());
    }
  }
}