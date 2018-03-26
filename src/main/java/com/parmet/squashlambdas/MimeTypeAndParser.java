package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

public class MimeTypeAndParser<T> {
  private final String mimeType;
  private final IoMsgFunction<? super BodyPart, ? extends Appendable2<T>> parser;

  public MimeTypeAndParser(
      String mimeType, IoMsgFunction<? super BodyPart, ? extends Appendable2<T>> parser) {
    this.mimeType = checkNotNull(mimeType, "mimeType");
    this.parser = checkNotNull(parser, "parser");
  }

  public boolean isFor(BodyPart bodyPart) throws MessagingException {
    return bodyPart.isMimeType(mimeType);
  }

  public Appendable2<T> parse(BodyPart bodyPart) throws IOException, MessagingException {
    return parser.apply(bodyPart);
  }
}
