package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.Iterables;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import org.apache.commons.mail.util.MimeMessageParser;

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
        MimeMessageParser parser = new MimeMessageParser(message);
        return new EmailData(
            parser.getSubject(),
            new BodyExtractor().getEventsFromMessage(message).toString(),
            Iterables.getOnlyElement(
                Iterables.getOnlyElement(
                    new CalendarExtractor().getEventsFromMessage(message).toList())
                .getEvents()));
      }
    });
  }
}
