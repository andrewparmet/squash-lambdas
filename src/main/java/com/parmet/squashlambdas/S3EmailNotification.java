package com.parmet.squashlambdas;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class S3EmailNotification {
  private String eventVersion;
  private String eventSource;
  private String awsRegion;
  private OffsetDateTime eventTime;
  private String eventName;
  private S3CreateObjectInfo s3;

  public S3CreateObjectInfo getS3ObjectInfo() {
    return s3;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("eventVersion", eventVersion)
        .append("eventSource", eventSource)
        .append("awsRegion", awsRegion)
        .append("eventTime", eventTime)
        .append("eventName", eventName)
        .append("s3", s3)
        .build();
  }
}
