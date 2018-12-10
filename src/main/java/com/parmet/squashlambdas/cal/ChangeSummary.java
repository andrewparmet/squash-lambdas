package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.parmet.squashlambdas.activity.Activity;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.util.Utils;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class ChangeSummary {
  private final Action action;
  private final Activity activity;

  ChangeSummary(Action action, Activity activity) {
    this.action = checkNotNull(action, "action");
    this.activity = checkNotNull(activity, "activity");
  }

  public static Optional<ChangeSummary> fromEmail(EmailData email) {
    return new ChangeSummaryRetriever(email).getChangeSummary();
  }

  public void process(EventManager eventManager) {
    Utils.run(() -> action.handle(activity, eventManager));
  }

  public Activity getSchedulable() {
    return activity;
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
        .append(activity, rhs.activity)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(action)
        .append(activity)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("action", action)
        .append("activity", activity)
        .build();
  }
}
