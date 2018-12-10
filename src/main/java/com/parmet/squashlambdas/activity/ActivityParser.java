package com.parmet.squashlambdas.activity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.activity.TimeParser.StartAndEnd;
import com.parmet.squashlambdas.email.EmailData;

class ActivityParser {
  private final EmailData email;

  public ActivityParser(EmailData email) {
    this.email = checkNotNull(email, "email");
  }

  public StartAndEnd parseStartAndEnd() {
    return TimeParser.parse(email.getBody());
  }

  public Court parseCourt() {
    return Court.fromLocationString(email.getBody());
  }

  public ImmutableSet<String> parseOtherPlayers() {
    return OtherPlayersParser.parse(email.getBody());
  }
}
