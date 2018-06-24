package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import biweekly.property.Attendee;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.match.TimeParser.StartAndEnd;
import java.util.List;
import java.util.Set;

class MatchRetriever {
  private final EmailData email;

  public MatchRetriever(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public Match getMatch() {
    System.out.println(email.getBody());
    StartAndEnd times = TimeParser.parse(email.getBody());
    return new Match(parseCourt(), parseOtherPlayers(), times.getStart(), times.getEnd());
  }

  private Court parseCourt() {
    return Court.fromLocationString(email.getBody());
  }

  private Set<String> parseOtherPlayers() {
    return OtherPlayersParser.parse(email.getBody());
  }

  static Set<String> notMe(List<Attendee> attendees) {
    return attendees
        .stream()
        .map(Attendee::getCommonName)
        .filter(not("Andrew Parmet"::equals))
        .collect(toImmutableSet());
  }
}
