package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.parmet.squashlambdas.Sport.HARDBALL;
import static com.parmet.squashlambdas.Sport.RACQUETS;
import static com.parmet.squashlambdas.Sport.SQUASH;
import static com.parmet.squashlambdas.Sport.TENNIS;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Court {
  COURT_1 (SQUASH),
  COURT_2 (SQUASH),
  COURT_3 (SQUASH),
  COURT_5 (HARDBALL),
  COURT_6 (HARDBALL),
  COURT_7 (HARDBALL),
  TENNIS_COURT (TENNIS),
  RACQUETS_COURT (RACQUETS);

  private static final Pattern LOCATION = Pattern.compile(".*Court: Court #(\\d)");

  private final Sport sport;

  private Court(Sport sport) {
    this.sport = checkNotNull(sport, "sport");
  }

  /** "Tennis & Racquet Club / Court: Court #7" */
  public static Court fromLocationString(String location) {
    Matcher m = LOCATION.matcher(location);
    checkArgument(m.matches(), "Unable to parse location from %s", location);
    int courtNum = Integer.parseInt(m.group(1));
    switch (courtNum) {
      case 1:
        return COURT_1;
      case 2:
        return COURT_2;
      case 3:
        return COURT_3;
      case 5:
        return COURT_5;
      case 6:
        return COURT_6;
      case 7:
        return COURT_7;
      default:
        throw new IllegalArgumentException(
            String.format("Unknown court number %s for location %s", courtNum, location));
    }
  }
  
  public Sport getSport() {
    return sport;
  }
}
