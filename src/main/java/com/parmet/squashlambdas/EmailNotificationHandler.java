package com.parmet.squashlambdas;

import static com.google.common.base.Throwables.throwIfUnchecked;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fatboyindustrial.gsonjavatime.InstantConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.time.Instant;

public class EmailNotificationHandler implements RequestHandler<Object, Object> {
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(Instant.class, new InstantConverter()).create();

  private static final AmazonS3 S3 =
      AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

  @Override
  public Object handleRequest(Object input, Context context) {
    try {
      S3EmailNotification note = fromInputObject(input);
      new EmailRetriever(
              S3, note.getS3ObjectInfo().getBucketName(), note.getS3ObjectInfo().getObjectKey())
          .retrieveEmail();
      return "Hello world; received: " + input;
    } catch (Exception ex) {
      throwIfUnchecked(ex);
      throw new RuntimeException(ex);
    }
  }

  static S3EmailNotification fromInputObject(Object input) {
    JsonElement json = GSON.toJsonTree(input);
    return GSON.fromJson(
        json.getAsJsonObject().getAsJsonArray("Records").get(0), S3EmailNotification.class);
  }
}
