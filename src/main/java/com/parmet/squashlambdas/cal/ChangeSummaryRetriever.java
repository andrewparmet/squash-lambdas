package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.match.Match;
import java.util.Optional;

class ChangeSummaryRetriever {
  private final EmailData email;

  public ChangeSummaryRetriever(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public Optional<ChangeSummary> getChangeSummary() {
    Action action = Action.parseFromSubject(email.getBody());

    if (action.equals(Action.NONE)) {
      return Optional.empty();
    } else {
      return Optional.of(new ChangeSummary(action, Match.getFromEmailData(email)));
    }
  }
}
