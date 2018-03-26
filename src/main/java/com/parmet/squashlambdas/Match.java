package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Match {
  enum Sport {
    SQUASH,
    TENNIS,
    RACQUETS
  }

  enum Court {
    COURT_1,
    COURT_2,
    COURT_3,
    COURT_4,
    COURT_5,
    COURT_6,
    COURT_7,
    TENNIS_COURT,
    RACQUETS_COURT
  }

  private final Sport sport;
  private final ImmutableSet<String> otherPlayers;
  private final Instant start;
  private final Instant end;
  private final Court court;

  public Match(Sport sport, Court court, Set<String> otherPlayers, Instant start, Instant end) {
    this.sport = checkNotNull(sport, "sport");
    this.court = checkNotNull(court, "court");
    this.otherPlayers = ImmutableSet.copyOf(otherPlayers);
    this.start = checkNotNull(start, "start");
    this.end = checkNotNull(end, "end");
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("sport", sport)
        .append("court", court)
        .append("otherPlayers", otherPlayers)
        .append("start", start)
        .append("end", end)
        .build();
  }
}
