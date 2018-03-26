package com.parmet.squashlambdas;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class S3CreateObjectInfo {
  private BucketInfo bucket;
  private S3ObjectInfo object;

  public String getBucketName() {
    return bucket.getName();
  }

  public String getObjectKey() {
    return object.getKey();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("bucket", bucket).append("object", object).build();
  }
}
