package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.email.EmailData;
import java.time.Instant;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Match {
  private final Instant createTime = Instant.now();

  private final ImmutableSet<String> otherPlayers;
  private final Instant start;
  private final Instant end;
  private final Court court;

  public Match(Court court, Set<String> otherPlayers, Instant start, Instant end) {
    this.court = checkNotNull(court, "court");
    this.otherPlayers = ImmutableSet.copyOf(otherPlayers);
    this.start = checkNotNull(start, "start");
    this.end = checkNotNull(end, "end");
  }

  public static Match getFromEmailData(EmailData email) {
    return new MatchRetriever(email).getMatch();
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public Court getCourt() {
    return court;
  }

  public Event toEvent() {
    return new Event()
        .setStart(new EventDateTime().setDateTime(new DateTime(start.toEpochMilli())))
        .setEnd(new EventDateTime().setDateTime(new DateTime(end.toEpochMilli())))
        .setLocation(court + ", Tennis and Racquet Club")
        .setSummary(court.getSport() + renderOtherPlayers())
        .setDescription(toString());
  }

  private String renderOtherPlayers() {
    return otherPlayers.isEmpty() ? " Match" : " v. " + Joiner.on(',').join(otherPlayers);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    Match rhs = (Match) obj;
    return new EqualsBuilder()
        .append(court, rhs.court)
        .append(otherPlayers, rhs.otherPlayers)
        .append(start, rhs.start)
        .append(end, rhs.end)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(court)
        .append(otherPlayers)
        .append(start)
        .append(end)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("createTime", createTime)
        .append("court", court)
        .append("otherPlayers", otherPlayers)
        .append("start", start)
        .append("end", end)
        .build();
  }
}
