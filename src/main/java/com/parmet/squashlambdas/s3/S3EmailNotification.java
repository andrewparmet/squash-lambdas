package com.parmet.squashlambdas.s3;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fatboyindustrial.gsonjavatime.InstantConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.time.Instant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class S3EmailNotification {
  private static final Gson GSON =
      new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantConverter())
        .create();

  private String eventVersion;
  private String eventSource;
  private String awsRegion;
  private Instant eventTime;
  private String eventName;
  private S3CreateObjectInfo s3;

  public S3EmailNotification(
      String eventVersion,
      String eventSource,
      String awsRegion,
      Instant eventTime,
      String eventName,
      S3CreateObjectInfo s3) {
    this.eventVersion = checkNotNull(eventVersion, "eventVersion");
    this.eventSource = checkNotNull(eventSource, "eventSource");
    this.awsRegion = checkNotNull(awsRegion, "awsRegion");
    this.eventTime = checkNotNull(eventTime, "eventTime");
    this.eventName = checkNotNull(eventName, "eventName");
    this.s3 = checkNotNull(s3, "s3");
  }

  public static S3EmailNotification fromInputObject(Object input) {
    JsonElement json = GSON.toJsonTree(input);
    return GSON.fromJson(
        json.getAsJsonObject().getAsJsonArray("Records").get(0), S3EmailNotification.class);
  }

  public S3CreateObjectInfo getS3ObjectInfo() {
    return s3;
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

    S3EmailNotification rhs = (S3EmailNotification) obj;
    return new EqualsBuilder()
        .append(eventVersion, rhs.eventVersion)
        .append(eventSource, rhs.eventSource)
        .append(awsRegion, rhs.awsRegion)
        .append(eventTime, rhs.eventTime)
        .append(eventName, rhs.eventName)
        .append(s3, rhs.s3)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(eventVersion)
        .append(eventSource)
        .append(awsRegion)
        .append(eventTime)
        .append(eventName)
        .append(s3)
        .build();
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
