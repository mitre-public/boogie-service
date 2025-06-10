package org.mitre.tdp.boogie.aws;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketLifecycleRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketLifecycleResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.DeletePublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.DeletePublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketAclRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAclResponse;
import software.amazon.awssdk.services.s3.model.GetBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.GetBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketNotificationConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketNotificationConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.GetBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusResponse;
import software.amazon.awssdk.services.s3.model.GetBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketRequestPaymentRequest;
import software.amazon.awssdk.services.s3.model.GetBucketRequestPaymentResponse;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAclResponse;
import software.amazon.awssdk.services.s3.model.GetObjectAttributesRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAttributesResponse;
import software.amazon.awssdk.services.s3.model.GetObjectLegalHoldRequest;
import software.amazon.awssdk.services.s3.model.GetObjectLegalHoldResponse;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRetentionRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRetentionResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTorrentRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTorrentResponse;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketAnalyticsConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketAnalyticsConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.ListBucketIntelligentTieringConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketIntelligentTieringConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.ListBucketInventoryConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketInventoryConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsRequest;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAclRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAclResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.PutBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.PutBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.PutBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketLoggingRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.PutBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketNotificationConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketNotificationConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.PutBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.PutBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketRequestPaymentRequest;
import software.amazon.awssdk.services.s3.model.PutBucketRequestPaymentResponse;
import software.amazon.awssdk.services.s3.model.PutBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.PutBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.PutBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectAclResponse;
import software.amazon.awssdk.services.s3.model.PutObjectLegalHoldRequest;
import software.amazon.awssdk.services.s3.model.PutObjectLegalHoldResponse;
import software.amazon.awssdk.services.s3.model.PutObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutObjectLockConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRetentionRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRetentionResponse;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.RestoreObjectRequest;
import software.amazon.awssdk.services.s3.model.RestoreObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Request;
import software.amazon.awssdk.services.s3.model.SelectObjectContentRequest;
import software.amazon.awssdk.services.s3.model.SelectObjectContentResponseHandler;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.model.WriteGetObjectResponseRequest;
import software.amazon.awssdk.services.s3.model.WriteGetObjectResponseResponse;
import software.amazon.awssdk.services.s3.paginators.ListMultipartUploadsPublisher;
import software.amazon.awssdk.services.s3.paginators.ListObjectVersionsPublisher;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;
import software.amazon.awssdk.services.s3.paginators.ListPartsPublisher;
import software.amazon.awssdk.services.s3.waiters.S3AsyncWaiter;

final class RateLimitedS3Client implements S3AsyncClient {

  private final S3AsyncClient client;
  private final RequestLimiters<S3Request> requestLimiters;

  private RateLimitedS3Client(
      S3AsyncClient client,
      RequestLimiters<S3Request> requestLimiters
  ) {
    this.client = requireNonNull(client);
    this.requestLimiters = requireNonNull(requestLimiters);
  }

  static S3AsyncClient withLimiters(S3AsyncClient client, RequestLimiters<S3Request> requestLimiters) {
    return new RateLimitedS3Client(client, requestLimiters);
  }

  @Override
  public String serviceName() {
    return client.serviceName();
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public CompletableFuture<AbortMultipartUploadResponse> abortMultipartUpload(AbortMultipartUploadRequest abortMultipartUploadRequest) {
    return client.abortMultipartUpload(requestLimiters.acquireToExecute(abortMultipartUploadRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(abortMultipartUploadRequest));
  }

  @Override
  public CompletableFuture<CompleteMultipartUploadResponse> completeMultipartUpload(CompleteMultipartUploadRequest completeMultipartUploadRequest) {
    return client.completeMultipartUpload(requestLimiters.acquireToExecute(completeMultipartUploadRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(completeMultipartUploadRequest));
  }

  @Override
  public CompletableFuture<CopyObjectResponse> copyObject(CopyObjectRequest copyObjectRequest) {
    return client.copyObject(requestLimiters.acquireToExecute(copyObjectRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(copyObjectRequest));
  }

  @Override
  public CompletableFuture<CreateBucketResponse> createBucket(CreateBucketRequest createBucketRequest) {
    return client.createBucket(requestLimiters.acquireToExecute(createBucketRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(createBucketRequest));
  }

  @Override
  public CompletableFuture<CreateMultipartUploadResponse> createMultipartUpload(CreateMultipartUploadRequest createMultipartUploadRequest) {
    return client.createMultipartUpload(requestLimiters.acquireToExecute(createMultipartUploadRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(createMultipartUploadRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketResponse> deleteBucket(DeleteBucketRequest deleteBucketRequest) {
    return client.deleteBucket(requestLimiters.acquireToExecute(deleteBucketRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketAnalyticsConfigurationResponse> deleteBucketAnalyticsConfiguration(DeleteBucketAnalyticsConfigurationRequest deleteBucketAnalyticsConfigurationRequest) {
    return client.deleteBucketAnalyticsConfiguration(requestLimiters.acquireToExecute(deleteBucketAnalyticsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketAnalyticsConfigurationRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketCorsResponse> deleteBucketCors(DeleteBucketCorsRequest deleteBucketCorsRequest) {
    return client.deleteBucketCors(requestLimiters.acquireToExecute(deleteBucketCorsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketCorsRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketEncryptionResponse> deleteBucketEncryption(DeleteBucketEncryptionRequest deleteBucketEncryptionRequest) {
    return client.deleteBucketEncryption(requestLimiters.acquireToExecute(deleteBucketEncryptionRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketEncryptionRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketIntelligentTieringConfigurationResponse> deleteBucketIntelligentTieringConfiguration(DeleteBucketIntelligentTieringConfigurationRequest deleteBucketIntelligentTieringConfigurationRequest) {
    return client.deleteBucketIntelligentTieringConfiguration(requestLimiters.acquireToExecute(deleteBucketIntelligentTieringConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketIntelligentTieringConfigurationRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketInventoryConfigurationResponse> deleteBucketInventoryConfiguration(DeleteBucketInventoryConfigurationRequest deleteBucketInventoryConfigurationRequest) {
    return client.deleteBucketInventoryConfiguration(requestLimiters.acquireToExecute(deleteBucketInventoryConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketInventoryConfigurationRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketLifecycleResponse> deleteBucketLifecycle(DeleteBucketLifecycleRequest deleteBucketLifecycleRequest) {
    return client.deleteBucketLifecycle(requestLimiters.acquireToExecute(deleteBucketLifecycleRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketLifecycleRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketMetricsConfigurationResponse> deleteBucketMetricsConfiguration(DeleteBucketMetricsConfigurationRequest deleteBucketMetricsConfigurationRequest) {
    return client.deleteBucketMetricsConfiguration(requestLimiters.acquireToExecute(deleteBucketMetricsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketMetricsConfigurationRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketOwnershipControlsResponse> deleteBucketOwnershipControls(DeleteBucketOwnershipControlsRequest deleteBucketOwnershipControlsRequest) {
    return client.deleteBucketOwnershipControls(requestLimiters.acquireToExecute(deleteBucketOwnershipControlsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketOwnershipControlsRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketPolicyResponse> deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) {
    return client.deleteBucketPolicy(requestLimiters.acquireToExecute(deleteBucketPolicyRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketPolicyRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketReplicationResponse> deleteBucketReplication(DeleteBucketReplicationRequest deleteBucketReplicationRequest) {
    return client.deleteBucketReplication(requestLimiters.acquireToExecute(deleteBucketReplicationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketReplicationRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketTaggingResponse> deleteBucketTagging(DeleteBucketTaggingRequest deleteBucketTaggingRequest) {
    return client.deleteBucketTagging(requestLimiters.acquireToExecute(deleteBucketTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketTaggingRequest));
  }

  @Override
  public CompletableFuture<DeleteBucketWebsiteResponse> deleteBucketWebsite(DeleteBucketWebsiteRequest deleteBucketWebsiteRequest) {
    return client.deleteBucketWebsite(requestLimiters.acquireToExecute(deleteBucketWebsiteRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteBucketWebsiteRequest));
  }

  @Override
  public CompletableFuture<DeleteObjectResponse> deleteObject(DeleteObjectRequest deleteObjectRequest) {
    return client.deleteObject(requestLimiters.acquireToExecute(deleteObjectRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteObjectRequest));
  }

  @Override
  public CompletableFuture<DeleteObjectTaggingResponse> deleteObjectTagging(DeleteObjectTaggingRequest deleteObjectTaggingRequest) {
    return client.deleteObjectTagging(requestLimiters.acquireToExecute(deleteObjectTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteObjectTaggingRequest));
  }

  @Override
  public CompletableFuture<DeleteObjectsResponse> deleteObjects(DeleteObjectsRequest deleteObjectsRequest) {
    return client.deleteObjects(requestLimiters.acquireToExecute(deleteObjectsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deleteObjectsRequest));
  }

  @Override
  public CompletableFuture<DeletePublicAccessBlockResponse> deletePublicAccessBlock(DeletePublicAccessBlockRequest deletePublicAccessBlockRequest) {
    return client.deletePublicAccessBlock(requestLimiters.acquireToExecute(deletePublicAccessBlockRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(deletePublicAccessBlockRequest));
  }

  @Override
  public CompletableFuture<GetBucketAccelerateConfigurationResponse> getBucketAccelerateConfiguration(GetBucketAccelerateConfigurationRequest getBucketAccelerateConfigurationRequest) {
    return client.getBucketAccelerateConfiguration(requestLimiters.acquireToExecute(getBucketAccelerateConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketAccelerateConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketAclResponse> getBucketAcl(GetBucketAclRequest getBucketAclRequest) {
    return client.getBucketAcl(requestLimiters.acquireToExecute(getBucketAclRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketAclRequest));
  }

  @Override
  public CompletableFuture<GetBucketAnalyticsConfigurationResponse> getBucketAnalyticsConfiguration(GetBucketAnalyticsConfigurationRequest getBucketAnalyticsConfigurationRequest) {
    return client.getBucketAnalyticsConfiguration(requestLimiters.acquireToExecute(getBucketAnalyticsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketAnalyticsConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketCorsResponse> getBucketCors(GetBucketCorsRequest getBucketCorsRequest) {
    return client.getBucketCors(requestLimiters.acquireToExecute(getBucketCorsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketCorsRequest));
  }

  @Override
  public CompletableFuture<GetBucketEncryptionResponse> getBucketEncryption(GetBucketEncryptionRequest getBucketEncryptionRequest) {
    return client.getBucketEncryption(requestLimiters.acquireToExecute(getBucketEncryptionRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketEncryptionRequest));
  }

  @Override
  public CompletableFuture<GetBucketIntelligentTieringConfigurationResponse> getBucketIntelligentTieringConfiguration(GetBucketIntelligentTieringConfigurationRequest getBucketIntelligentTieringConfigurationRequest) {
    return client.getBucketIntelligentTieringConfiguration(requestLimiters.acquireToExecute(getBucketIntelligentTieringConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketIntelligentTieringConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketInventoryConfigurationResponse> getBucketInventoryConfiguration(GetBucketInventoryConfigurationRequest getBucketInventoryConfigurationRequest) {
    return client.getBucketInventoryConfiguration(requestLimiters.acquireToExecute(getBucketInventoryConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketInventoryConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketLifecycleConfigurationResponse> getBucketLifecycleConfiguration(GetBucketLifecycleConfigurationRequest getBucketLifecycleConfigurationRequest) {
    return client.getBucketLifecycleConfiguration(requestLimiters.acquireToExecute(getBucketLifecycleConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketLifecycleConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketLocationResponse> getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) {
    return client.getBucketLocation(requestLimiters.acquireToExecute(getBucketLocationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketLocationRequest));
  }

  @Override
  public CompletableFuture<GetBucketLoggingResponse> getBucketLogging(GetBucketLoggingRequest getBucketLoggingRequest) {
    return client.getBucketLogging(requestLimiters.acquireToExecute(getBucketLoggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketLoggingRequest));
  }

  @Override
  public CompletableFuture<GetBucketMetricsConfigurationResponse> getBucketMetricsConfiguration(GetBucketMetricsConfigurationRequest getBucketMetricsConfigurationRequest) {
    return client.getBucketMetricsConfiguration(requestLimiters.acquireToExecute(getBucketMetricsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketMetricsConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketNotificationConfigurationResponse> getBucketNotificationConfiguration(GetBucketNotificationConfigurationRequest getBucketNotificationConfigurationRequest) {
    return client.getBucketNotificationConfiguration(requestLimiters.acquireToExecute(getBucketNotificationConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketNotificationConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetBucketOwnershipControlsResponse> getBucketOwnershipControls(GetBucketOwnershipControlsRequest getBucketOwnershipControlsRequest) {
    return client.getBucketOwnershipControls(requestLimiters.acquireToExecute(getBucketOwnershipControlsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketOwnershipControlsRequest));
  }

  @Override
  public CompletableFuture<GetBucketPolicyResponse> getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) {
    return client.getBucketPolicy(requestLimiters.acquireToExecute(getBucketPolicyRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketPolicyRequest));
  }

  @Override
  public CompletableFuture<GetBucketPolicyStatusResponse> getBucketPolicyStatus(GetBucketPolicyStatusRequest getBucketPolicyStatusRequest) {
    return client.getBucketPolicyStatus(requestLimiters.acquireToExecute(getBucketPolicyStatusRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketPolicyStatusRequest));
  }

  @Override
  public CompletableFuture<GetBucketReplicationResponse> getBucketReplication(GetBucketReplicationRequest getBucketReplicationRequest) {
    return client.getBucketReplication(requestLimiters.acquireToExecute(getBucketReplicationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketReplicationRequest));
  }

  @Override
  public CompletableFuture<GetBucketRequestPaymentResponse> getBucketRequestPayment(GetBucketRequestPaymentRequest getBucketRequestPaymentRequest) {
    return client.getBucketRequestPayment(requestLimiters.acquireToExecute(getBucketRequestPaymentRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketRequestPaymentRequest));
  }

  @Override
  public CompletableFuture<GetBucketTaggingResponse> getBucketTagging(GetBucketTaggingRequest getBucketTaggingRequest) {
    return client.getBucketTagging(requestLimiters.acquireToExecute(getBucketTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketTaggingRequest));
  }

  @Override
  public CompletableFuture<GetBucketVersioningResponse> getBucketVersioning(GetBucketVersioningRequest getBucketVersioningRequest) {
    return client.getBucketVersioning(requestLimiters.acquireToExecute(getBucketVersioningRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketVersioningRequest));
  }

  @Override
  public CompletableFuture<GetBucketWebsiteResponse> getBucketWebsite(GetBucketWebsiteRequest getBucketWebsiteRequest) {
    return client.getBucketWebsite(requestLimiters.acquireToExecute(getBucketWebsiteRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getBucketWebsiteRequest));
  }

  @Override
  public <ReturnT> CompletableFuture<ReturnT> getObject(GetObjectRequest getObjectRequest, AsyncResponseTransformer<GetObjectResponse, ReturnT> asyncResponseTransformer) {
    return client.getObject(requestLimiters.acquireToExecute(getObjectRequest), asyncResponseTransformer)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectRequest));
  }

  @Override
  public CompletableFuture<GetObjectResponse> getObject(GetObjectRequest getObjectRequest, Path destinationPath) {
    return client.getObject(requestLimiters.acquireToExecute(getObjectRequest), destinationPath)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectRequest));
  }

  @Override
  public CompletableFuture<GetObjectAclResponse> getObjectAcl(GetObjectAclRequest getObjectAclRequest) {
    return client.getObjectAcl(requestLimiters.acquireToExecute(getObjectAclRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectAclRequest));
  }

  @Override
  public CompletableFuture<GetObjectAttributesResponse> getObjectAttributes(GetObjectAttributesRequest getObjectAttributesRequest) {
    return client.getObjectAttributes(requestLimiters.acquireToExecute(getObjectAttributesRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectAttributesRequest));
  }

  @Override
  public CompletableFuture<GetObjectLegalHoldResponse> getObjectLegalHold(GetObjectLegalHoldRequest getObjectLegalHoldRequest) {
    return client.getObjectLegalHold(requestLimiters.acquireToExecute(getObjectLegalHoldRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectLegalHoldRequest));
  }

  @Override
  public CompletableFuture<GetObjectLockConfigurationResponse> getObjectLockConfiguration(GetObjectLockConfigurationRequest getObjectLockConfigurationRequest) {
    return client.getObjectLockConfiguration(requestLimiters.acquireToExecute(getObjectLockConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectLockConfigurationRequest));
  }

  @Override
  public CompletableFuture<GetObjectRetentionResponse> getObjectRetention(GetObjectRetentionRequest getObjectRetentionRequest) {
    return client.getObjectRetention(requestLimiters.acquireToExecute(getObjectRetentionRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectRetentionRequest));
  }

  @Override
  public CompletableFuture<GetObjectTaggingResponse> getObjectTagging(GetObjectTaggingRequest getObjectTaggingRequest) {
    return client.getObjectTagging(requestLimiters.acquireToExecute(getObjectTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectTaggingRequest));
  }

  @Override
  public <ReturnT> CompletableFuture<ReturnT> getObjectTorrent(GetObjectTorrentRequest getObjectTorrentRequest, AsyncResponseTransformer<GetObjectTorrentResponse, ReturnT> asyncResponseTransformer) {
    return client.getObjectTorrent(requestLimiters.acquireToExecute(getObjectTorrentRequest), asyncResponseTransformer)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectTorrentRequest));
  }

  @Override
  public CompletableFuture<GetObjectTorrentResponse> getObjectTorrent(GetObjectTorrentRequest getObjectTorrentRequest, Path destinationPath) {
    return client.getObjectTorrent(requestLimiters.acquireToExecute(getObjectTorrentRequest), destinationPath)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getObjectTorrentRequest));
  }

  @Override
  public CompletableFuture<GetPublicAccessBlockResponse> getPublicAccessBlock(GetPublicAccessBlockRequest getPublicAccessBlockRequest) {
    return client.getPublicAccessBlock(requestLimiters.acquireToExecute(getPublicAccessBlockRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(getPublicAccessBlockRequest));
  }

  @Override
  public CompletableFuture<HeadBucketResponse> headBucket(HeadBucketRequest headBucketRequest) {
    return client.headBucket(requestLimiters.acquireToExecute(headBucketRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(headBucketRequest));
  }

  @Override
  public CompletableFuture<HeadObjectResponse> headObject(HeadObjectRequest headObjectRequest) {
    return client.headObject(requestLimiters.acquireToExecute(headObjectRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(headObjectRequest));
  }

  @Override
  public CompletableFuture<ListBucketAnalyticsConfigurationsResponse> listBucketAnalyticsConfigurations(ListBucketAnalyticsConfigurationsRequest listBucketAnalyticsConfigurationsRequest) {
    return client.listBucketAnalyticsConfigurations(requestLimiters.acquireToExecute(listBucketAnalyticsConfigurationsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listBucketAnalyticsConfigurationsRequest));
  }

  @Override
  public CompletableFuture<ListBucketIntelligentTieringConfigurationsResponse> listBucketIntelligentTieringConfigurations(ListBucketIntelligentTieringConfigurationsRequest listBucketIntelligentTieringConfigurationsRequest) {
    return client.listBucketIntelligentTieringConfigurations(requestLimiters.acquireToExecute(listBucketIntelligentTieringConfigurationsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listBucketIntelligentTieringConfigurationsRequest));
  }

  @Override
  public CompletableFuture<ListBucketInventoryConfigurationsResponse> listBucketInventoryConfigurations(ListBucketInventoryConfigurationsRequest listBucketInventoryConfigurationsRequest) {
    return client.listBucketInventoryConfigurations(requestLimiters.acquireToExecute(listBucketInventoryConfigurationsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listBucketInventoryConfigurationsRequest));
  }

  @Override
  public CompletableFuture<ListBucketMetricsConfigurationsResponse> listBucketMetricsConfigurations(ListBucketMetricsConfigurationsRequest listBucketMetricsConfigurationsRequest) {
    return client.listBucketMetricsConfigurations(requestLimiters.acquireToExecute(listBucketMetricsConfigurationsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listBucketMetricsConfigurationsRequest));
  }

  @Override
  public CompletableFuture<ListBucketsResponse> listBuckets(ListBucketsRequest listBucketsRequest) {
    return client.listBuckets(requestLimiters.acquireToExecute(listBucketsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listBucketsRequest));
  }

  @Override
  public CompletableFuture<ListMultipartUploadsResponse> listMultipartUploads(ListMultipartUploadsRequest listMultipartUploadsRequest) {
    return client.listMultipartUploads(requestLimiters.acquireToExecute(listMultipartUploadsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listMultipartUploadsRequest));
  }

  @Override
  public ListMultipartUploadsPublisher listMultipartUploadsPaginator(ListMultipartUploadsRequest listMultipartUploadsRequest) {
    ListMultipartUploadsPublisher publisher = client.listMultipartUploadsPaginator(requestLimiters.acquireToExecute(listMultipartUploadsRequest));
    publisher.doAfterOnCancel(() -> requestLimiters.releaseOnCompletion(listMultipartUploadsRequest));
    publisher.doAfterOnComplete(() -> requestLimiters.releaseOnCompletion(listMultipartUploadsRequest));
    publisher.doAfterOnError(throwable -> requestLimiters.releaseOnCompletion(listMultipartUploadsRequest));
    return publisher;
  }

  @Override
  public CompletableFuture<ListObjectVersionsResponse> listObjectVersions(ListObjectVersionsRequest listObjectVersionsRequest) {
    return client.listObjectVersions(requestLimiters.acquireToExecute(listObjectVersionsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listObjectVersionsRequest));
  }

  @Override
  public ListObjectVersionsPublisher listObjectVersionsPaginator(ListObjectVersionsRequest listObjectVersionsRequest) {
    ListObjectVersionsPublisher publisher = client.listObjectVersionsPaginator(requestLimiters.acquireToExecute(listObjectVersionsRequest));
    publisher.doAfterOnCancel(() -> requestLimiters.releaseOnCompletion(listObjectVersionsRequest));
    publisher.doAfterOnComplete(() -> requestLimiters.releaseOnCompletion(listObjectVersionsRequest));
    publisher.doAfterOnError(throwable -> requestLimiters.releaseOnCompletion(listObjectVersionsRequest));
    return publisher;
  }

  @Override
  public CompletableFuture<ListObjectsResponse> listObjects(ListObjectsRequest listObjectsRequest) {
    return client.listObjects(requestLimiters.acquireToExecute(listObjectsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listObjectsRequest));
  }

  @Override
  public CompletableFuture<ListObjectsV2Response> listObjectsV2(ListObjectsV2Request listObjectsV2Request) {
    return client.listObjectsV2(requestLimiters.acquireToExecute(listObjectsV2Request))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listObjectsV2Request));
  }

  @Override
  public ListObjectsV2Publisher listObjectsV2Paginator(ListObjectsV2Request listObjectsV2Request) {
    ListObjectsV2Publisher publisher = client.listObjectsV2Paginator(requestLimiters.acquireToExecute(listObjectsV2Request));
    publisher.doAfterOnCancel(() -> requestLimiters.releaseOnCompletion(listObjectsV2Request));
    publisher.doAfterOnComplete(() -> requestLimiters.releaseOnCompletion(listObjectsV2Request));
    publisher.doAfterOnError(throwable -> requestLimiters.releaseOnCompletion(listObjectsV2Request));
    return publisher;
  }

  @Override
  public CompletableFuture<ListPartsResponse> listParts(ListPartsRequest listPartsRequest) {
    return client.listParts(requestLimiters.acquireToExecute(listPartsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(listPartsRequest));
  }

  @Override
  public ListPartsPublisher listPartsPaginator(ListPartsRequest listPartsRequest) {
    ListPartsPublisher publisher = client.listPartsPaginator(requestLimiters.acquireToExecute(listPartsRequest));
    publisher.doAfterOnCancel(() -> requestLimiters.releaseOnCompletion(listPartsRequest));
    publisher.doAfterOnComplete(() -> requestLimiters.releaseOnCompletion(listPartsRequest));
    publisher.doAfterOnError(throwable -> requestLimiters.releaseOnCompletion(listPartsRequest));
    return publisher;
  }

  @Override
  public CompletableFuture<PutBucketAccelerateConfigurationResponse> putBucketAccelerateConfiguration(PutBucketAccelerateConfigurationRequest putBucketAccelerateConfigurationRequest) {
    return client.putBucketAccelerateConfiguration(requestLimiters.acquireToExecute(putBucketAccelerateConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketAccelerateConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketAclResponse> putBucketAcl(PutBucketAclRequest putBucketAclRequest) {
    return client.putBucketAcl(requestLimiters.acquireToExecute(putBucketAclRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketAclRequest));
  }

  @Override
  public CompletableFuture<PutBucketAnalyticsConfigurationResponse> putBucketAnalyticsConfiguration(PutBucketAnalyticsConfigurationRequest putBucketAnalyticsConfigurationRequest) {
    return client.putBucketAnalyticsConfiguration(requestLimiters.acquireToExecute(putBucketAnalyticsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketAnalyticsConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketCorsResponse> putBucketCors(PutBucketCorsRequest putBucketCorsRequest) {
    return client.putBucketCors(requestLimiters.acquireToExecute(putBucketCorsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketCorsRequest));
  }

  @Override
  public CompletableFuture<PutBucketEncryptionResponse> putBucketEncryption(PutBucketEncryptionRequest putBucketEncryptionRequest) {
    return client.putBucketEncryption(requestLimiters.acquireToExecute(putBucketEncryptionRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketEncryptionRequest));
  }

  @Override
  public CompletableFuture<PutBucketIntelligentTieringConfigurationResponse> putBucketIntelligentTieringConfiguration(PutBucketIntelligentTieringConfigurationRequest putBucketIntelligentTieringConfigurationRequest) {
    return client.putBucketIntelligentTieringConfiguration(requestLimiters.acquireToExecute(putBucketIntelligentTieringConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketIntelligentTieringConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketInventoryConfigurationResponse> putBucketInventoryConfiguration(PutBucketInventoryConfigurationRequest putBucketInventoryConfigurationRequest) {
    return client.putBucketInventoryConfiguration(requestLimiters.acquireToExecute(putBucketInventoryConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketInventoryConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketLifecycleConfigurationResponse> putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest putBucketLifecycleConfigurationRequest) {
    return client.putBucketLifecycleConfiguration(requestLimiters.acquireToExecute(putBucketLifecycleConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketLifecycleConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketLoggingResponse> putBucketLogging(PutBucketLoggingRequest putBucketLoggingRequest) {
    return client.putBucketLogging(requestLimiters.acquireToExecute(putBucketLoggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketLoggingRequest));
  }

  @Override
  public CompletableFuture<PutBucketMetricsConfigurationResponse> putBucketMetricsConfiguration(PutBucketMetricsConfigurationRequest putBucketMetricsConfigurationRequest) {
    return client.putBucketMetricsConfiguration(requestLimiters.acquireToExecute(putBucketMetricsConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketMetricsConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketNotificationConfigurationResponse> putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest putBucketNotificationConfigurationRequest) {
    return client.putBucketNotificationConfiguration(requestLimiters.acquireToExecute(putBucketNotificationConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketNotificationConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutBucketOwnershipControlsResponse> putBucketOwnershipControls(PutBucketOwnershipControlsRequest putBucketOwnershipControlsRequest) {
    return client.putBucketOwnershipControls(requestLimiters.acquireToExecute(putBucketOwnershipControlsRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketOwnershipControlsRequest));
  }

  @Override
  public CompletableFuture<PutBucketPolicyResponse> putBucketPolicy(PutBucketPolicyRequest putBucketPolicyRequest) {
    return client.putBucketPolicy(requestLimiters.acquireToExecute(putBucketPolicyRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketPolicyRequest));
  }

  @Override
  public CompletableFuture<PutBucketReplicationResponse> putBucketReplication(PutBucketReplicationRequest putBucketReplicationRequest) {
    return client.putBucketReplication(requestLimiters.acquireToExecute(putBucketReplicationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketReplicationRequest));
  }

  @Override
  public CompletableFuture<PutBucketRequestPaymentResponse> putBucketRequestPayment(PutBucketRequestPaymentRequest putBucketRequestPaymentRequest) {
    return client.putBucketRequestPayment(requestLimiters.acquireToExecute(putBucketRequestPaymentRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketRequestPaymentRequest));
  }

  @Override
  public CompletableFuture<PutBucketTaggingResponse> putBucketTagging(PutBucketTaggingRequest putBucketTaggingRequest) {
    return client.putBucketTagging(requestLimiters.acquireToExecute(putBucketTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketTaggingRequest));
  }

  @Override
  public CompletableFuture<PutBucketVersioningResponse> putBucketVersioning(PutBucketVersioningRequest putBucketVersioningRequest) {
    return client.putBucketVersioning(requestLimiters.acquireToExecute(putBucketVersioningRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketVersioningRequest));
  }

  @Override
  public CompletableFuture<PutBucketWebsiteResponse> putBucketWebsite(PutBucketWebsiteRequest putBucketWebsiteRequest) {
    return client.putBucketWebsite(requestLimiters.acquireToExecute(putBucketWebsiteRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putBucketWebsiteRequest));
  }

  @Override
  public CompletableFuture<PutObjectResponse> putObject(PutObjectRequest putObjectRequest, AsyncRequestBody requestBody) {
    return client.putObject(requestLimiters.acquireToExecute(putObjectRequest), requestBody)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectRequest));
  }

  @Override
  public CompletableFuture<PutObjectResponse> putObject(PutObjectRequest putObjectRequest, Path sourcePath) {
    return client.putObject(requestLimiters.acquireToExecute(putObjectRequest), sourcePath)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectRequest));
  }

  @Override
  public CompletableFuture<PutObjectAclResponse> putObjectAcl(PutObjectAclRequest putObjectAclRequest) {
    return client.putObjectAcl(requestLimiters.acquireToExecute(putObjectAclRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectAclRequest));
  }

  @Override
  public CompletableFuture<PutObjectLegalHoldResponse> putObjectLegalHold(PutObjectLegalHoldRequest putObjectLegalHoldRequest) {
    return client.putObjectLegalHold(requestLimiters.acquireToExecute(putObjectLegalHoldRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectLegalHoldRequest));
  }

  @Override
  public CompletableFuture<PutObjectLockConfigurationResponse> putObjectLockConfiguration(PutObjectLockConfigurationRequest putObjectLockConfigurationRequest) {
    return client.putObjectLockConfiguration(requestLimiters.acquireToExecute(putObjectLockConfigurationRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectLockConfigurationRequest));
  }

  @Override
  public CompletableFuture<PutObjectRetentionResponse> putObjectRetention(PutObjectRetentionRequest putObjectRetentionRequest) {
    return client.putObjectRetention(requestLimiters.acquireToExecute(putObjectRetentionRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectRetentionRequest));
  }

  @Override
  public CompletableFuture<PutObjectTaggingResponse> putObjectTagging(PutObjectTaggingRequest putObjectTaggingRequest) {
    return client.putObjectTagging(requestLimiters.acquireToExecute(putObjectTaggingRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putObjectTaggingRequest));
  }

  @Override
  public CompletableFuture<PutPublicAccessBlockResponse> putPublicAccessBlock(PutPublicAccessBlockRequest putPublicAccessBlockRequest) {
    return client.putPublicAccessBlock(requestLimiters.acquireToExecute(putPublicAccessBlockRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(putPublicAccessBlockRequest));
  }

  @Override
  public CompletableFuture<RestoreObjectResponse> restoreObject(RestoreObjectRequest restoreObjectRequest) {
    return client.restoreObject(requestLimiters.acquireToExecute(restoreObjectRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(restoreObjectRequest));
  }

  @Override
  public CompletableFuture<Void> selectObjectContent(SelectObjectContentRequest selectObjectContentRequest, SelectObjectContentResponseHandler asyncResponseHandler) {
    return client.selectObjectContent(requestLimiters.acquireToExecute(selectObjectContentRequest), asyncResponseHandler)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(selectObjectContentRequest));
  }

  @Override
  public CompletableFuture<UploadPartResponse> uploadPart(UploadPartRequest uploadPartRequest, AsyncRequestBody requestBody) {
    return client.uploadPart(requestLimiters.acquireToExecute(uploadPartRequest), requestBody)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(uploadPartRequest));
  }

  @Override
  public CompletableFuture<UploadPartResponse> uploadPart(UploadPartRequest uploadPartRequest, Path sourcePath) {
    return client.uploadPart(requestLimiters.acquireToExecute(uploadPartRequest), sourcePath)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(uploadPartRequest));
  }

  @Override
  public CompletableFuture<UploadPartCopyResponse> uploadPartCopy(UploadPartCopyRequest uploadPartCopyRequest) {
    return client.uploadPartCopy(requestLimiters.acquireToExecute(uploadPartCopyRequest))
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(uploadPartCopyRequest));
  }

  @Override
  public CompletableFuture<WriteGetObjectResponseResponse> writeGetObjectResponse(WriteGetObjectResponseRequest writeGetObjectResponseRequest, AsyncRequestBody requestBody) {
    return client.writeGetObjectResponse(requestLimiters.acquireToExecute(writeGetObjectResponseRequest), requestBody)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(writeGetObjectResponseRequest));
  }

  @Override
  public CompletableFuture<WriteGetObjectResponseResponse> writeGetObjectResponse(WriteGetObjectResponseRequest writeGetObjectResponseRequest, Path sourcePath) {
    return client.writeGetObjectResponse(requestLimiters.acquireToExecute(writeGetObjectResponseRequest), sourcePath)
        .whenComplete((response, throwable) -> requestLimiters.releaseOnCompletion(writeGetObjectResponseRequest));
  }

  @Override
  public S3Utilities utilities() {
    return client.utilities();
  }

  @Override
  public S3AsyncWaiter waiter() {
    return client.waiter();
  }
}