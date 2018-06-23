package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.match.Match;

class ChangeSummaryRetriever {
  private final EmailData email;

  public ChangeSummaryRetriever(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public ChangeSummary getChangeSummary() {
    return new ChangeSummary(
        Action.parseFromSubject(email.getSubject()),
        Match.getFromEmailData(email));
  }
}
