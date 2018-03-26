package com.parmet.squashlambdas;

import static java.nio.charset.StandardCharsets.UTF_8;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.util.List;

public class CalendarExtractor extends AbstractMimeMessageExtractor<ICalendar> {
  private static final ImmutableList<MimeTypeAndParser<ICalendar>> TYPE_AND_PARSERS =
      ImmutableList.of(
          new MimeTypeAndParser<>(
              "text/calendar",
              bodyPart -> {
                try (InputStreamReader isr =
                    new InputStreamReader(bodyPart.getInputStream(), UTF_8)) {
                  String str = CharStreams.toString(isr);
                  return new AppendableList<ICalendar>(Biweekly.parse(str).all());
                }
              }));

  protected CalendarExtractor() {
    super(AppendableList::new);
  }

  @Override
  protected List<MimeTypeAndParser<ICalendar>> typeAndParsers() {
    return TYPE_AND_PARSERS;
  }
}
