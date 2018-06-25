package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.google.common.io.CharStreams;
import com.parmet.squashlambdas.email.EmailUtils.IoMsgFunction;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.mail.MessagingException;
import javax.mail.Part;
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
                InputStreamReader isr = new InputStreamReader(is, UTF_8)) {
              return new AppendableList<>(Biweekly.parse(CharStreams.toString(isr)).all());
            }
          });

  private final String mimeType;
  private final IoMsgFunction<? super Part, ? extends Appendable2<T>> parser;

  private MimeParser(
      String mimeType, IoMsgFunction<? super Part, ? extends Appendable2<T>> parser) {
    this.mimeType = checkNotNull(mimeType, "mimeType");
    this.parser = checkNotNull(parser, "parser");
  }

  public boolean isFor(Part part) throws MessagingException {
    return part.isMimeType(mimeType);
  }

  public Appendable2<T> parse(Part part) throws IOException, MessagingException {
    checkArgument(
        isFor(part),
        "Cannot parse part with type %s using parser for type %s",
        part.getContentType(),
        mimeType);
    return parser.apply(part);
  }
}
