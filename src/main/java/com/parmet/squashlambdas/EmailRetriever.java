package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import biweekly.Biweekly;
import biweekly.ICalendar;
import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import net.fortuna.ical4j.data.ParserException;
import org.apache.commons.mail.util.MimeMessageParser;
import org.jsoup.Jsoup;

public class EmailRetriever {
  private final AmazonS3 s3;
  private final S3EmailNotification note;

  public EmailRetriever(AmazonS3 s3, S3EmailNotification note) {
    this.s3 = checkNotNull(s3, "s3");
    this.note = checkNotNull(note, "note");
  }

  public EmailData retrieveEmail() throws Exception {
    String email =
        s3.getObjectAsString(
            note.getS3ObjectInfo().getBucketName(), note.getS3ObjectInfo().getObjectKey());
    System.setProperty(
        "net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
    try (InputStream is = new ByteArrayInputStream(email.getBytes())) {
      MimeMessage message = new MimeMessage(null, is);
      MimeMessageParser parser = new MimeMessageParser(message);
      return new EmailData(
          parser.getSubject(),
          getTextFromMessage(message),
          Iterables.getOnlyElement(
              Iterables.getOnlyElement(getEventsFromMessage(message)).getEvents()));
    }
  }

  private List<ICalendar> getEventsFromMessage(MimeMessage message)
      throws MessagingException, IOException, ParserException {
    if (message.isMimeType("text/calendar")) {
      return null;
    } else if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      return getEventsFromMimeMultipart(mimeMultipart);
    } else {
      return ImmutableList.of();
    }
  }

  private List<ICalendar> getEventsFromMimeMultipart(MimeMultipart mimeMultipart)
      throws MessagingException, IOException, ParserException {
    int count = mimeMultipart.getCount();
    if (count == 0) {
      throw new MessagingException("Multipart with no body parts not supported.");
    }
    boolean multipartAlt =
        new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
    if (multipartAlt)
      // Alternatives appear in an order of increasing faithfulness to the original content.
      return getEventFromBodyPart(mimeMultipart.getBodyPart(count - 1));
    List<ICalendar> l = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      l.addAll(getEventFromBodyPart(bodyPart));
    }
    return l;
  }

  private List<ICalendar> getEventFromBodyPart(BodyPart bodyPart)
      throws IOException, ParserException, MessagingException {
    if (bodyPart.isMimeType("text/calendar")) {
      try (InputStreamReader isr = new InputStreamReader(bodyPart.getInputStream())) {
        String str = CharStreams.toString(isr);
        return Biweekly.parse(str).all();
      }
    } else {
      return ImmutableList.of();
    }
  }

  private String getTextFromMessage(Message message) throws IOException, MessagingException {
    if (message.isMimeType("text/plain")) {
      return message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      return getTextFromMimeMultipart(mimeMultipart);
    } else {
      return "";
    }
  }

  private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)
      throws IOException, MessagingException {
    int count = mimeMultipart.getCount();
    if (count == 0) {
      throw new MessagingException("Multipart with no body parts not supported.");
    }
    boolean multipartAlt =
        new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
    if (multipartAlt)
      // Alternatives appear in an order of increasing faithfulness to the original content.
      return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      result.append(getTextFromBodyPart(bodyPart));
    }
    return result.toString();
  }

  private String getTextFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
    if (bodyPart.isMimeType("text/plain")) {
      return (String) bodyPart.getContent();
    } else if (bodyPart.isMimeType("text/html")) {
      String html = (String) bodyPart.getContent();
      return Jsoup.parse(html).text();
    } else if (bodyPart.getContent() instanceof MimeMultipart) {
      return getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
    } else {
      return "";
    }
  }
}
