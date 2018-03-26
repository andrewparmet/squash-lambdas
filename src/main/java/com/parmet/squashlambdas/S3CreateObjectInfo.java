package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class S3CreateObjectInfo {
  private BucketInfo bucket;
  private S3ObjectInfo object;

  public S3CreateObjectInfo(BucketInfo bucket, S3ObjectInfo object) {
    this.bucket = checkNotNull(bucket, "bucket");
    this.object = checkNotNull(object, "object");
  }

  public String getBucketName() {
    return bucket.getName();
  }

  public String getObjectKey() {
    return object.getKey();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    S3CreateObjectInfo rhs = (S3CreateObjectInfo) obj;
    return new EqualsBuilder().append(bucket, rhs.bucket).append(object, rhs.object).build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(bucket).append(object).build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("bucket", bucket).append("object", object).build();
  }
}
