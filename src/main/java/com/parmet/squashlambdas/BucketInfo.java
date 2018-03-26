package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BucketInfo {
  private String name;

  public BucketInfo(String name) {
    this.name = checkNotNull(name, "name");
  }

  public String getName() {
    return name;
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

    BucketInfo rhs = (BucketInfo) obj;
    return new EqualsBuilder().append(name, rhs.name).build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).build();
  }
}
