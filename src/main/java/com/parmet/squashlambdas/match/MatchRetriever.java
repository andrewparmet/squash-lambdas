package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import biweekly.property.Attendee;
import com.parmet.squashlambdas.email.EmailData;
import java.util.List;
import java.util.Set;

class MatchRetriever {
  private final EmailData email;

  public MatchRetriever(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public Match getMatch() {
    return new Match(null, null, null, null);
  }

  static Set<String> notMe(List<Attendee> attendees) {
    return attendees
        .stream()
        .map(Attendee::getCommonName)
        .filter(not("Andrew Parmet"::equals))
        .collect(toImmutableSet());
  }
}
