package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

abstract class AbstractMimeMessageExtractor<T> {
  private Supplier<? extends Appendable2<T>> newInstance;

  protected AbstractMimeMessageExtractor(Supplier<? extends Appendable2<T>> newInstance) {
    this.newInstance = checkNotNull(newInstance);
  }

  public Appendable2<T> getEventsFromMessage(MimeMessage message) {
    return Utils.wrap(() -> {
      if (message.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        return getFromMimeMultipart(mimeMultipart);
      } else {
        return getFromPart(message);
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
      return newInstance.get().appendAll(getFromPart(mimeMultipart.getBodyPart(count - 1)));
    }
    Appendable2<T> t = newInstance.get();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      t.appendAll(getFromPart(bodyPart));
    }
    return t;
  }

  private Appendable2<T> getFromPart(Part part) throws IOException, MessagingException {
    if (part.getContent() instanceof MimeMultipart) {
      return getFromMimeMultipart((MimeMultipart) part.getContent());
    }

    for (MimeParser<T> typeAndParser : parsers()) {
      if (typeAndParser.isFor(part)) {
        return newInstance.get().appendAll(typeAndParser.parse(part));
      }
    }

    return newInstance.get();
  }

  protected abstract List<MimeParser<T>> parsers();
}
