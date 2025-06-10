package org.mitre.tdp.boogie.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.mitre.caasd.commons.util.DemotedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.utils.Either;

/**
 * Creates a mapping from various AWS SDK request types made by a client to a shared {@link RateLimiter} for that type of request
 * to prevent overwhelming the AWS SDK with requests and causing a large number of "limit exceeded" exceptions in clients.
 *
 * <p>This can be in conjunction with a decorator pattern approach to wrap AWS {@link SdkClient} implementations with built-in
 * rate-limiting functionality so client classes of those clients need not write their own rate-limiting logic internally.
 *
 * <p>This can be used with :https://docs.aws.amazon.com/general/latest/gr/aws-service-information.html, to decorate provided clients
 * with their appropriate specified rate limits.
 */
@SuppressWarnings("UnstableApiUsage")
public final class RequestLimiters<S extends SdkRequest> {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLimiters.class);

  private static final Duration MAX_QUEUE_WAIT = Duration.ofMinutes(5);

  /**
   * {@link ConcurrentHashMap} containing a mapping from request type to the associated {@link RateLimiter} for request of that
   * type.
   */
  private final ConcurrentHashMap<Class<? extends SdkRequest>, Either<RateLimiter, Semaphore>> requestLimiters;
  /**
   * The maximum duration any given call should have to wait before we throw a hard exception.
   *
   * <p>I.e. the queue has grown too big and requests are still coming in too quickly (we aren't smoothing out a localized spike
   * in requests) and therefore something else should be done besides growing the size of the queue indefinitely.
   */
  private final Duration maxQueueWaitTime;

  private RequestLimiters(Builder<S> builder) {
    this.requestLimiters = new ConcurrentHashMap<>(builder.requestLimiters);
    this.maxQueueWaitTime = ofNullable(builder.maxQueueWaitTime).orElse(MAX_QUEUE_WAIT);
    checkArgument(!maxQueueWaitTime.isNegative() && !maxQueueWaitTime.isZero(), "Should be >0");
  }

  /**
   * Returns a new {@link Builder} which, by default, will force all requests to happen synchronously.
   */
  public static <S extends SdkRequest> Builder<S> builder() {
    return new Builder<>();
  }

  /**
   * Attempts to acquire a permit from either the configured {@link RateLimiter} or {@link Semaphore} associated with the request
   * type. Note that if one is not configured it will "grant a permit" immediately and this method won't block.
   *
   * <p>Returns the provided request for easier/nicer chaining of this method into the execution of a request.
   */
  public <R extends S> R acquireToExecute(R request) {
    requireNonNull(request, "Provided request cannot be null.");

    requestLimiters.computeIfPresent(request.getClass(), (k, v) -> {
      // Note: RateLimiter.acquire() & Semaphore.acquire() are threadSafe
      v.apply(this::acquireFromRateLimiter, this::acquireFromSemaphore);
      return v;
    });
    return request;
  }

  private void acquireFromRateLimiter(RateLimiter rateLimiter) {
    checkArgument(rateLimiter.acquire() <= maxQueueWaitTime.getSeconds(),
        "Request blocked longer than max time - check request submissions.");
  }

  private void acquireFromSemaphore(Semaphore semaphore) {
    Stopwatch watch = Stopwatch.createStarted();
    try {
      semaphore.acquire();
      checkArgument(watch.elapsed(TimeUnit.SECONDS) <= maxQueueWaitTime.getSeconds());
    } catch (InterruptedException e) {
      throw DemotedException.demote("Thread interrupted while waiting to acquire semaphore permit.", e);
    }
  }

  public <R extends S> void releaseOnCompletion(R request) {
    requestLimiters.computeIfPresent(request.getClass(), (k, v) -> {
      v.apply(rateLimiter -> {}, Semaphore::release);
      return v;
    });
  }

  public static final class Builder<S extends SdkRequest> {

    private final Map<Class<? extends S>, Either<RateLimiter, Semaphore>> requestLimiters;
    private Duration maxQueueWaitTime;

    private Builder() {
      this.requestLimiters = new HashMap<>();
    }

    /**
     * The maximum time a request can be in the queue before the {@link RequestLimiters} throws a hard exception.
     */
    public Builder<S> maxQueueWaitTime(Duration maxQueueWaitTime) {
      this.maxQueueWaitTime = maxQueueWaitTime;
      return this;
    }

    /**
     * Sets the requests per second cap for requests of the provided type.
     *
     * <p>Has a default warmup period of 100ms.
     */
    public Builder<S> limitRequestsPerSecondFor(Class<? extends S> requestType, double requestsPerSecond) {
      return limitRequestsPerSecondFor(requestType, requestsPerSecond, Duration.ZERO);
    }

    /**
     * Sets the requests per second cap for requests of the provided type.
     *
     * <p>Allows providing a warmup period for the rate limiter so that requests are smoothly scaled up to the max amount and
     * requests don't "start hot" which can occasionally lead to 'LimitExceededException's.
     *
     * <p>Note: this warmupPeriod can be set to 0 for no smooth buildup.
     */
    public Builder<S> limitRequestsPerSecondFor(Class<? extends S> requestType, double requestsPerSecond, Duration warmupPeriod) {
      requestLimiters.put(requestType, Either.left(RateLimiter.create(requestsPerSecond, warmupPeriod.toMillis(), TimeUnit.MILLISECONDS)));
      return this;
    }

    /**
     * Sets a fixed size connection pool for requests - limiting the total number of requests of a given type that can be in flight
     * at the same time.
     *
     * <p>Deprecated because of the failing unit test... I think this should work... but it does not. Someone in the future may clown
     * me over this still existing and being broken. It's ok I deserve it. :sob:
     */
    @Deprecated
    public Builder<S> limitMaxInFlightRequestsTo(Class<? extends S> requestType, int maxInFlightRequests) {
      requestLimiters.put(requestType, Either.right(new Semaphore(maxInFlightRequests)));
      return this;
    }

    public RequestLimiters<S> build() {
      return new RequestLimiters<>(this);
    }
  }
}
