package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Match {
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
        .append("court", court)
        .append("otherPlayers", otherPlayers)
        .append("start", start)
        .append("end", end)
        .build();
  }
}
