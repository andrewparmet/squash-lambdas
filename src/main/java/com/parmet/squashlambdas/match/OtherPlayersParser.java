package com.parmet.squashlambdas.match;

import com.google.common.collect.ImmutableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class OtherPlayersParser {
  private static final Logger log = LogManager.getLogger();

  private static final Pattern HAS_JOINED =
      Pattern.compile(".*Parmet, (.*) has joined your reservation.*");

  private static final Pattern OTHER_PLAYERS =
      Pattern.compile(".* Other players in the slot: (.*) Court:.*");

  // Name can be followed by an email in parentheses.
  private static final Pattern WITH =
      Pattern.compile(".* With: (.*) (\\(.*\\))? Court:.*");

  public static ImmutableSet<String> parse(String body) {
    Matcher hasJoined = HAS_JOINED.matcher(body);
    if (hasJoined.matches()) {
      return ImmutableSet.of(hasJoined.group(1));
    } else {
      Matcher otherPlayers = OTHER_PLAYERS.matcher(body);
      if (otherPlayers.matches()) {
        return ImmutableSet.of(otherPlayers.group(1));
      } else {
        Matcher with = WITH.matcher(body);
        if (with.matches()) {
          return ImmutableSet.of(with.group(1));
        } else {
          log.info("Did not find other players in body {}", body);
          return ImmutableSet.of();
        }
      }
    }
  }
}
