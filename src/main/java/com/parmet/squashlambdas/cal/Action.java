package com.parmet.squashlambdas.cal;

import com.parmet.squashlambdas.match.Match;
import java.io.IOException;
import java.util.stream.Stream;

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

  public static Action parseFromSubject(String body) {
    if (Stream.of(
        "A reservation including you has been made",
        "You've joined a reservation").anyMatch(body::contains)) {
      return CREATE;
    }
    if (Stream.of(
        "has been removed",
        "has joined your reservation").anyMatch(body::contains)) {
      return UPDATE;
    }
    if (Stream.of(
        "You've been removed from a reservation",
        "You have successfully cancelled a reservation").anyMatch(body::contains)) {
      return DELETE;
    }

    if (Stream.of(
        "This is a reminder").anyMatch(body::contains)) {
      return NONE;
    }

    throw new IllegalArgumentException("Unable to parse action from " + body);
  }

  public abstract void handle(Match match, EventManager manager) throws IOException;
}
