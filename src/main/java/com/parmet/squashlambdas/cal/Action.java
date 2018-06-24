package com.parmet.squashlambdas.cal;

import java.util.stream.Stream;

public enum Action {
  CREATE, UPDATE, DELETE;

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
        "You've been removed from a reservation").anyMatch(body::contains)) {
      return DELETE;
    }

    throw new IllegalArgumentException();
  }
}
