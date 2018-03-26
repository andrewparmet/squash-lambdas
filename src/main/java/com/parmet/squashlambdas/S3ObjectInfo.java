package com.parmet.squashlambdas;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class S3ObjectInfo {
  private String key;

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("key", key).build();
  }
}
