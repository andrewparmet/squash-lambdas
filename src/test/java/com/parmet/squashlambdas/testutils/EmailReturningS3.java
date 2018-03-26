package com.parmet.squashlambdas.testutils;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bizo.awsstubs.services.s3.AmazonS3Stub;

public class EmailReturningS3 extends AmazonS3Stub {
  private final String object;

  public EmailReturningS3(String object) {
    this.object = checkNotNull(object, "object");
  }

  @Override
  public String getObjectAsString(String bucket, String key) {
    return object;
  }
}
