package com.parmet.squashlambdas;

import biweekly.ICalendar;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class CalendarExtractor extends AbstractMimeMessageExtractor<ICalendar> {
  private static final ImmutableList<MimeParser<ICalendar>> TYPE_AND_PARSERS =
      ImmutableList.of(MimeParser.TEXT_CALENDAR);

  protected CalendarExtractor() {
    super(AppendableList::new);
  }

  @Override
  protected List<MimeParser<ICalendar>> parsers() {
    return TYPE_AND_PARSERS;
  }
}
