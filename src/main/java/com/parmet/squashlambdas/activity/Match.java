package com.parmet.squashlambdas.activity;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.activity.TimeParser.StartAndEnd;
import com.parmet.squashlambdas.email.EmailData;
import java.time.Instant;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class Match extends AbstractActivity {
  private final ImmutableSet<String> otherPlayers;

  public Match(Court court, Instant start, Instant end, Set<String> otherPlayers) {
    super(court, start, end);
    this.otherPlayers = ImmutableSet.copyOf(otherPlayers);
  }

  public static Match fromEmailData(EmailData email) {
    ActivityParser retriever = new ActivityParser(email);
    StartAndEnd startAndEnd = retriever.parseStartAndEnd();
    return new Match(
        retriever.parseCourt(),
        startAndEnd.getStart(),
        startAndEnd.getEnd(),
        retriever.parseOtherPlayers());
  }
  
  @Override
  protected String summary() {
    return getCourt().getSport() + renderOtherPlayers();
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
        .appendSuper(super.equals(obj))
        .append(otherPlayers, rhs.otherPlayers)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .appendSuper(super.hashCode())
        .append(otherPlayers)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("otherPlayers", otherPlayers)
        .build();
  }
}
