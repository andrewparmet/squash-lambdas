package com.parmet.squashlambdas.cal;

public enum Action {
  CREATE, UPDATE, DELETE;

  public static Action parseFromSubject(String subject) {
    if (subject.contains("A reservation including you has been made")) {
      return CREATE;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
