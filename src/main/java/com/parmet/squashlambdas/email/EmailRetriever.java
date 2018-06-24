package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import biweekly.ICalendar;
import com.amazonaws.services.s3.AmazonS3;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.mail.internet.MimeMessage;

public class EmailRetriever {
  private final AmazonS3 s3;
  private final String bucket;
  private final String key;

  public EmailRetriever(AmazonS3 s3, String bucket, String key) {
    this.s3 = checkNotNull(s3, "s3");
    this.bucket = checkNotNull(bucket, "bucket");
    this.key = checkNotNull(key, "key");
  }

  public EmailData retrieveEmail() {
    return Utils.wrap(() -> {
      String email = s3.getObjectAsString(bucket, key);
      try (InputStream is = new ByteArrayInputStream(email.getBytes(UTF_8))) {
        MimeMessage message = new MimeMessage(null, is);
        return new EmailData(
            message.getSubject(),
            new BodyExtractor().getEventsFromMessage(message).toString(),
            new CalendarExtractor()
                .getEventsFromMessage(message)
                .toList()
                .stream()
                .map(ICalendar::getEvents)
                .flatMap(List::stream)
                .findFirst());
      }
    });
  }
}
