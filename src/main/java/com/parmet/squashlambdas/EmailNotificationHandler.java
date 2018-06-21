package com.parmet.squashlambdas;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fatboyindustrial.gsonjavatime.InstantConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.match.MatchRetriever;
import com.parmet.squashlambdas.s3.S3EmailNotification;
import java.time.Instant;

public class EmailNotificationHandler implements RequestHandler<Object, Object> {
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(Instant.class, new InstantConverter()).create();

  private static final AmazonS3 S3 =
      AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

  @Override
  public Object handleRequest(Object input, Context context) {
    S3EmailNotification note = fromInputObject(input);

    Match match = new MatchRetriever(S3, note).getMatch();

    // Todo: Google API calls
    // 1. If created, create
    // 2. If deleted, delete
    // 3. If player added, delete and create new

    // Catch exception and email myself failure

    return "Hello world; received: " + input;
  }

  static S3EmailNotification fromInputObject(Object input) {
    JsonElement json = GSON.toJsonTree(input);
    return GSON.fromJson(
        json.getAsJsonObject().getAsJsonArray("Records").get(0), S3EmailNotification.class);
  }
}
