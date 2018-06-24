package com.parmet.squashlambdas;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.parmet.squashlambdas.cal.ChangeSummary;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetriever;
import com.parmet.squashlambdas.s3.S3CreateObjectInfo;
import com.parmet.squashlambdas.s3.S3EmailNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailNotificationHandler implements RequestHandler<Object, Object> {
  private static final Logger log = LogManager.getLogger();

  private static final AmazonS3 S3 =
      AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

  @Override
  public Object handleRequest(Object input, Context context) {
    try {
      S3CreateObjectInfo info = S3EmailNotification.fromInputObject(input).getS3ObjectInfo();
      EmailData email =
          new EmailRetriever(S3, info.getBucketName(), info.getObjectKey()).retrieveEmail();
      ChangeSummary changeSummary = ChangeSummary.fromEmail(email);

      // TODO: Google API calls
      // 1. If created, create
      // 2. If deleted, delete
      // 3. If player added, delete and create new

      // Catch exception and email myself failure

      log.info(changeSummary);
    } catch (Throwable t) {
      String msg = String.format("Caught error while processing input %s", input);
      log.error(msg, t);
    }

    return "Hello world; received: " + input;
  }
}
