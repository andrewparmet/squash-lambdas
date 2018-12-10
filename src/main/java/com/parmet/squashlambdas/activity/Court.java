package com.parmet.squashlambdas.activity;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.parmet.squashlambdas.activity.Sport.HARDBALL;
import static com.parmet.squashlambdas.activity.Sport.RACQUETS;
import static com.parmet.squashlambdas.activity.Sport.SQUASH;
import static com.parmet.squashlambdas.activity.Sport.TENNIS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Court {
  COURT_1(SQUASH),
  COURT_2(SQUASH),
  COURT_3(SQUASH),
  COURT_5(HARDBALL),
  COURT_6(HARDBALL),
  COURT_7(HARDBALL),
  TENNIS_COURT(TENNIS),
  RACQUETS_COURT(RACQUETS);

  // "Court: Court #x" (match creation)
  // "Courts: Court #x" (activity creation)
  private static final Pattern COURT = Pattern.compile(".*Courts?: Court #(\\d) [/\\-].*");

  private final Sport sport;
  private final String pretty =
      UPPER_UNDERSCORE.to(UPPER_CAMEL, name().replaceAll("_", " "));

  private Court(Sport sport) {
    this.sport = checkNotNull(sport, "sport");
  }

  /** e.g., "Tennis & Racquet Club / Court: Court #7" */
  public static Court fromLocationString(String body) {
    Matcher m = COURT.matcher(body);
    checkArgument(m.matches(), "Unable to parse court from %s", body);
    return valueOf("COURT_" + Integer.parseInt(m.group(1)));
  }

  public Sport getSport() {
    return sport;
  }

  @Override
  public String toString() {
    return pretty;
  }
}
