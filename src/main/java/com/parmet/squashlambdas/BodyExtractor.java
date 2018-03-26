package com.parmet.squashlambdas;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jsoup.Jsoup;

public class BodyExtractor extends AbstractMimeMessageExtractor<StringBuilder> {
  private static final ImmutableList<MimeTypeAndParser<StringBuilder>> TYPE_AND_PARSERS =
      ImmutableList.of(
          new MimeTypeAndParser<>(
              "text/plain",
              bodyPart -> new AppendableString(new StringBuilder((String) bodyPart.getContent()))),
          new MimeTypeAndParser<>(
              "text/html",
              bodyPart ->
                  new AppendableString(
                      new StringBuilder(Jsoup.parse((String) bodyPart.getContent()).text()))));

  protected BodyExtractor() {
    super(AppendableString::new);
  }

  @Override
  protected List<MimeTypeAndParser<StringBuilder>> typeAndParsers() {
    return TYPE_AND_PARSERS;
  }
}
