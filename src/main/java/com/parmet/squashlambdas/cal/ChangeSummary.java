package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.match.Match;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ChangeSummary {
  private final Action action;
  private final Match match;

  ChangeSummary(Action action, Match match) {
    this.action = checkNotNull(action, "action");
    this.match = checkNotNull(match, "match");
  }

  public static ChangeSummary fromEmail(EmailData email) {
    return new ChangeSummaryRetriever(email).getChangeSummary();
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

    ChangeSummary rhs = (ChangeSummary) obj;
    return new EqualsBuilder()
        .append(action, rhs.action)
        .append(match, rhs.match)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(action)
        .append(match)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("action", action)
        .append("match", match)
        .build();
  }
}