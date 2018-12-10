package com.parmet.squashlambdas.activity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.time.Instant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

abstract class AbstractActivity implements Activity {
  private final Instant createTime = Instant.now();

  private final Instant start;
  private final Instant end;
  private final Court court;

  public AbstractActivity(Court court, Instant start, Instant end) {
    this.court = checkNotNull(court, "court");
    this.start = checkNotNull(start, "start");
    this.end = checkNotNull(end, "end");
  }

  public final Instant getStart() {
    return start;
  }

  public final Instant getEnd() {
    return end;
  }

  public final Court getCourt() {
    return court;
  }
  
  @Override
  public String searchString() {
    return start + " " + end + " " + court;
  }

  @Override
  public final Event toEvent() {
    return new Event()
        .setStart(new EventDateTime().setDateTime(new DateTime(start.toEpochMilli())))
        .setEnd(new EventDateTime().setDateTime(new DateTime(end.toEpochMilli())))
        .setLocation(court + ", Tennis and Racquet Club")
        .setSummary(summary())
        .setDescription(toString());
  }
  
  protected abstract String summary();

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AbstractActivity)) {
      return false;
    }

    AbstractActivity rhs = (AbstractActivity) obj;
    return new EqualsBuilder()
        .append(court, rhs.court)
        .append(start, rhs.start)
        .append(end, rhs.end)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(court)
        .append(start)
        .append(end)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("createTime", createTime)
        .append("court", court)
        .append("start", start)
        .append("end", end)
        .build();
  }
}
