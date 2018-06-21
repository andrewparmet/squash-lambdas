package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import org.jsoup.Jsoup;

class MimeParser<T> {
  public static final MimeParser<StringBuilder> TEXT_PLAIN =
      new MimeParser<>(
          "text/plain",
          bodyPart -> new AppendableString(new StringBuilder((String) bodyPart.getContent())));

  public static final MimeParser<StringBuilder> TEXT_HTML =
      new MimeParser<>(
          "text/html",
          bodyPart ->
              new AppendableString(
                  new StringBuilder(Jsoup.parse((String) bodyPart.getContent()).text())));

  public static final MimeParser<ICalendar> TEXT_CALENDAR =
      new MimeParser<>(
          "text/calendar",
          bodyPart -> {
            try (InputStream is = bodyPart.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
              String str = CharStreams.toString(isr);
              return new AppendableList<>(Biweekly.parse(str).all());
            }
          });

  private final String mimeType;
  private final IoMsgFunction<? super BodyPart, ? extends Appendable2<T>> parser;

  private MimeParser(
      String mimeType, IoMsgFunction<? super BodyPart, ? extends Appendable2<T>> parser) {
    this.mimeType = checkNotNull(mimeType, "mimeType");
    this.parser = checkNotNull(parser, "parser");
  }

  public boolean isFor(BodyPart bodyPart) throws MessagingException {
    return bodyPart.isMimeType(mimeType);
  }

  public Appendable2<T> parse(BodyPart bodyPart) throws IOException, MessagingException {
    checkArgument(
        isFor(bodyPart),
        "Cannot parse bodyPart with type %s using parser for type %s",
        bodyPart.getContentType(),
        mimeType);
    return parser.apply(bodyPart);
  }
}
