package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.function.Supplier;
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

  public Appendable2<T> extract(MimeMessage message) {
    return EmailUtils.get(() -> {
      if (message.isMimeType("multipart/*")) {
        return getFromMimeMultipart((MimeMultipart) message.getContent());
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
    if (new ContentType(mimeMultipart.getContentType()).match("multipart/alternative")) {
      // Alternatives appear in order of increasing faithfulness to the original content.
      return getFromPart(mimeMultipart.getBodyPart(count - 1));
    }
    Appendable2<T> t = newInstance.get();
    for (int i = 0; i < count; i++) {
      t.appendAll(getFromPart(mimeMultipart.getBodyPart(i)));
    }
    return t;
  }

  private Appendable2<T> getFromPart(Part part) throws IOException, MessagingException {
    for (MimeParser<T> typeAndParser : parsers()) {
      if (typeAndParser.isFor(part)) {
        return typeAndParser.parse(part);
      }
    }
    return newInstance.get();
  }

  /** Parsers specified in descending preferred parse order (most preferred first). */
  protected abstract Iterable<MimeParser<T>> parsers();
}
