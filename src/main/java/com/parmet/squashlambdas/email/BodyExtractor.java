package com.parmet.squashlambdas.email;

import com.google.common.collect.ImmutableList;
import java.util.List;

class BodyExtractor extends AbstractMimeMessageExtractor<StringBuilder> {
  private static final ImmutableList<MimeParser<StringBuilder>> TYPE_AND_PARSERS =
      ImmutableList.of(MimeParser.TEXT_PLAIN, MimeParser.TEXT_HTML);

  protected BodyExtractor() {
    super(AppendableString::new);
  }

  @Override
  protected List<MimeParser<StringBuilder>> parsers() {
    return TYPE_AND_PARSERS;
  }
}
