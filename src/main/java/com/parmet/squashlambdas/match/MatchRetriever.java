package com.parmet.squashlambdas.match;

import static com.google.common.base.Preconditions.checkNotNull;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.match.TimeParser.StartAndEnd;
import java.util.Set;

class MatchRetriever {
  private final EmailData email;

  public MatchRetriever(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public Match getMatch() {
    StartAndEnd times = TimeParser.parse(email.getBody());
    return new Match(parseCourt(), parseOtherPlayers(), times.getStart(), times.getEnd());
  }

  private Court parseCourt() {
    return Court.fromLocationString(email.getBody());
  }

  private Set<String> parseOtherPlayers() {
    return OtherPlayersParser.parse(email.getBody());
  }
}
