package com.parmet.squashlambdas;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BucketInfo {
  private String name;

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).build();
  }
}
