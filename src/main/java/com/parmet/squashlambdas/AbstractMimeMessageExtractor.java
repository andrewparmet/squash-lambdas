package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public abstract class AbstractMimeMessageExtractor<T> {
  private Supplier<? extends Appendable2<T>> newInstance;

  protected AbstractMimeMessageExtractor(Supplier<? extends Appendable2<T>> newInstance) {
    this.newInstance = checkNotNull(newInstance);
  }

  public Appendable2<T> getEventsFromMessage(MimeMessage message) {
    return Utils.wrap(() -> {
      if (message.isMimeType("text/calendar")) {
        return null;
      } else if (message.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        return getFromMimeMultipart(mimeMultipart);
      } else {
        return newInstance.get();
      }
    });
  }

  private Appendable2<T> getFromMimeMultipart(MimeMultipart mimeMultipart)
      throws IOException, MessagingException {
    int count = mimeMultipart.getCount();
    if (count == 0) {
      throw new MessagingException("Multipart with no body parts not supported.");
    }
    boolean multipartAlt =
        new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
    if (multipartAlt) {
      // Alternatives appear in an order of increasing faithfulness to the original content.
      return newInstance.get().appendAll(getFromBodyPart(mimeMultipart.getBodyPart(count - 1)));
    }
    Appendable2<T> t = newInstance.get();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      t.appendAll(getFromBodyPart(bodyPart));
    }
    return t;
  }

  private Appendable2<T> getFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
    if (bodyPart.getContent() instanceof MimeMultipart) {
      return getFromMimeMultipart((MimeMultipart) bodyPart.getContent());
    }

    for (MimeParser<T> typeAndParser : parsers()) {
      if (typeAndParser.isFor(bodyPart)) {
        return newInstance.get().appendAll(typeAndParser.parse(bodyPart));
      }
    }

    return newInstance.get();
  }

  protected abstract List<MimeParser<T>> parsers();
}
