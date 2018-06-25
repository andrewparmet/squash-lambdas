package com.parmet.squashlambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.google.api.services.calendar.Calendar;
import com.parmet.squashlambdas.cal.ChangeSummary;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetriever;
import com.parmet.squashlambdas.s3.S3CreateObjectInfo;
import com.parmet.squashlambdas.s3.S3EmailNotification;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailNotificationHandler implements RequestHandler<Object, Object> {
  private static final Logger log = LogManager.getLogger();

  private final AmazonS3 s3;
  private final Calendar calendar;

  public EmailNotificationHandler() {
    Configuration config = ConfigUtils.loadConfiguration(System.getenv("CONFIG_NAME") + ".xml");
    this.s3 = ConfigUtils.configureS3();
    this.calendar = ConfigUtils.configureCalendar(config, s3);
  }

  @Override
  public Object handleRequest(Object input, Context context) {
    try {
      S3CreateObjectInfo info = S3EmailNotification.fromInputObject(input).getS3ObjectInfo();
      EmailData email =
          new EmailRetriever(s3, info.getBucketName(), info.getObjectKey()).retrieveEmail();
      ChangeSummary.fromEmail(email).ifPresent(summary -> summary.process(calendar));
    } catch (Throwable t) {
      String msg = String.format("Caught error while processing input %s", input);
      log.error(msg, t);
    }

    return input;
  }
}
