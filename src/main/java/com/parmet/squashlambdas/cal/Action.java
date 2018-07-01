package com.parmet.squashlambdas.cal;

import com.google.common.collect.ImmutableList;
import com.parmet.squashlambdas.match.Match;
import java.io.IOException;
import java.util.Collection;

public enum Action {
  CREATE {
    @Override
    public void handle(Match match, EventManager manager) throws IOException {
      manager.create(match);
    }
  },
  UPDATE {
    @Override
    public void handle(Match match, EventManager manager) throws IOException {
      manager.update(match);
    }
  },
  DELETE {
    @Override
    public void handle(Match match, EventManager manager) throws IOException {
      manager.delete(match);
    }
  },
  NONE {
    @Override
    public void handle(Match match, EventManager manager) {
    }
  };

  private static final ImmutableList<String> CREATION =
      ImmutableList.of(
          "A reservation including you has been made",
          "You've joined a reservation");

  private static final ImmutableList<String> UPDATING =
      ImmutableList.of(
          "has been removed",
          "has joined your reservation");

  private static final ImmutableList<String> DELETION =
      ImmutableList.of(
          "You've been removed from a reservation",
          "You have successfully cancelled a reservation");

  private static final ImmutableList<String> NO_ACTION =
      ImmutableList.of(
          "This is a reminder");

  public static Action parseFromSubject(String body) {
    if (containsMatch(body, CREATION)) {
      return CREATE;
    }
    if (containsMatch(body, UPDATING)) {
      return UPDATE;
    }
    if (containsMatch(body, DELETION)) {
      return DELETE;
    }
    if (containsMatch(body, NO_ACTION)) {
      return NONE;
    }

    throw new IllegalArgumentException("Unable to parse action from " + body);
  }

  private static boolean containsMatch(String body, Collection<String> matches) {
    return matches.stream().anyMatch(body::contains);
  }

  public abstract void handle(Match match, EventManager manager) throws IOException;
}
