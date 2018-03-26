package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class S3ObjectInfo {
  private String key;

  public S3ObjectInfo(String key) {
    this.key = checkNotNull(key, "key");
  }

  public String getKey() {
    return key;
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

    S3ObjectInfo rhs = (S3ObjectInfo) obj;
    return new EqualsBuilder().append(key, rhs.key).build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(key).build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("key", key).build();
  }
}
