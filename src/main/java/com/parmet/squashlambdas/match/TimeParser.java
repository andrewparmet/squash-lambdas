package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TimeParser {
  /** "Date: Thursday, June 28th 2018 Time: 08:15 PM to 09:00 PM". */
  private static final Pattern PATTERN =
      Pattern.compile(".* Date: .*, (.*) (\\d{2}).* (.*) Time: (.*) to (.*) (AM|PM).*");

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a");

  public static StartAndEnd parse(String body) {
    Matcher matcher = PATTERN.matcher(body);
    checkArgument(matcher.matches(), "Unable to find start and end from body %s", body);
    Month month = Month.valueOf(matcher.group(1).toUpperCase());
    int dayOfMonth = Integer.parseInt(matcher.group(2));
    int year = Integer.parseInt(matcher.group(3));
    LocalTime startTime = LocalTime.parse(matcher.group(4), TIME);
    LocalTime endTime = LocalTime.parse(matcher.group(5) + " " + matcher.group(6), TIME);

    LocalDateTime start = LocalDateTime.of(
                LocalDate.of(year, month, dayOfMonth),
                LocalTime.of(startTime.getHour(), startTime.getMinute()));
    LocalDateTime end = LocalDateTime.of(
                LocalDate.of(year, month, dayOfMonth),
                LocalTime.of(endTime.getHour(), endTime.getMinute()));

    ZoneId offset = ZoneId.of("America/New_York");

    return new StartAndEnd(
        OffsetDateTime.of(start, offset.getRules().getOffset(start)).toInstant(),
        OffsetDateTime.of(end, offset.getRules().getOffset(end)).toInstant());
  }

  static class StartAndEnd {
    private final Instant start;
    private final Instant end;

    StartAndEnd(Instant start, Instant end) {
      this.start = checkNotNull(start, "start");
      this.end = checkNotNull(end, "end");
    }

    public Instant getStart() {
      return start;
    }

    public Instant getEnd() {
      return end;
    }
  }
}
