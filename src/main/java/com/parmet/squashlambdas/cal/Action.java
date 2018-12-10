package com.parmet.squashlambdas.cal;

import com.google.common.collect.ImmutableList;
import com.parmet.squashlambdas.activity.Activity;
import java.io.IOException;
import java.util.Collection;

public enum Action {
  CREATE {
    @Override
    public void handle(Activity activity, EventManager manager) throws IOException {
      manager.create(activity);
    }
  },
  UPDATE {
    @Override
    public void handle(Activity activity, EventManager manager) throws IOException {
      manager.update(activity);
    }
  },
  DELETE {
    @Override
    public void handle(Activity activity, EventManager manager) throws IOException {
      manager.delete(activity);
    }
  },
  NONE {
    @Override
    public void handle(Activity activity, EventManager manager) {
    }
  };

  private static final ImmutableList<String> CREATION =
      ImmutableList.of(
          "A reservation including you has been made",
          "You've joined a reservation",
          "You have been added to the activity");

  private static final ImmutableList<String> UPDATING =
      ImmutableList.of(
          "has been removed",
          "has joined your reservation");

  private static final ImmutableList<String> DELETION =
      ImmutableList.of(
          "You've been removed from a reservation",
          "You have successfully cancelled a reservation",
          "A reservation including you has been cancelled");

  private static final ImmutableList<String> NO_ACTION =
      ImmutableList.of(
          "This is a reminder",
          "Here are your scores recorded",
          "has cancelled your reservation",
          "has re-confirmed a reservation made");

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

  public abstract void handle(Activity activity, EventManager manager) throws IOException;
}
