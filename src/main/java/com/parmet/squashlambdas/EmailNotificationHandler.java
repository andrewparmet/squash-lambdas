package com.parmet.squashlambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.fatboyindustrial.gsonjavatime.InstantConverter;
import com.google.api.services.calendar.Calendar;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parmet.squashlambdas.cal.ChangeSummary;
import com.parmet.squashlambdas.cal.EventManager;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetriever;
import com.parmet.squashlambdas.s3.S3CreateObjectInfo;
import com.parmet.squashlambdas.s3.S3EmailNotification;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailNotificationHandler implements RequestHandler<Object, Object> {
  private static final Logger log = LogManager.getLogger();

  private static final Gson PRINTER =
      new GsonBuilder()
          .registerTypeAdapter(Instant.class, new InstantConverter())
          .serializeNulls()
          .setPrettyPrinting()
          .create();

  private static final Map<String, Object> context = new ConcurrentSkipListMap<>();

  private final AmazonS3 s3;
  private final AmazonSNS sns;
  private final Calendar calendar;

  private final String calendarId;
  private final String topicArn;

  public EmailNotificationHandler() {
    Configuration config = ConfigUtils.loadConfiguration(System.getenv("CONFIG_NAME") + ".xml");
    this.s3 = ConfigUtils.configureS3();
    this.sns = ConfigUtils.configureSns();
    this.calendar = ConfigUtils.configureCalendar(config, s3);
    this.calendarId = config.getString("google.calendarId");
    this.topicArn = config.getString("aws.handledTopicArn");
  }

  @Override
  public Object handleRequest(Object input, Context ignore) {
    log.info("Starting handling of {}", input);
    addToContext("input", input);
    try {
      S3CreateObjectInfo info = getS3Info(input);
      EmailData email = getEmail(info);
      ChangeSummary.fromEmail(email).ifPresent(summary -> {
        addToContext("changeSummary", summary);
        summary.process(new EventManager(calendar, calendarId));
        publishSuccess(summary);
      });
      return input;
    } catch (Throwable t) {
      log.error(t);
      publishFailure(t);
      throw t;
    }
  }

  private S3CreateObjectInfo getS3Info(Object input) {
    S3CreateObjectInfo info = S3EmailNotification.fromInputObject(input).getS3ObjectInfo();
    addToContext("s3CreateObjectInfo", info);
    return info;
  }

  private EmailData getEmail(S3CreateObjectInfo info) {
    EmailData email =
        new EmailRetriever(s3, info.getBucketName(), info.getObjectKey()).retrieveEmail();
    addToContext("emailData", email);
    return email;
  }

  private void publishSuccess(ChangeSummary summary) {
    sns.publish(
        topicArn,
        String.format("Successfuly processed change:\n"
            + "%s",
            PRINTER.toJson(summary)),
        "Processed Club Locker Email");
  }

  private void publishFailure(Throwable t) {
    sns.publish(
        topicArn,
        String.format("Encountered an error processing a Club Locker email:\n"
            + "Context:\n%s"
            + "\n"
            + "Stack trace:\n%s",
            PRINTER.toJson(context), Throwables.getStackTraceAsString(t)),
        "Failed to Process Club Locker Email");
  }

  public static void addToContext(String key, Object val) {
    context.put(key, val);
  }
}
