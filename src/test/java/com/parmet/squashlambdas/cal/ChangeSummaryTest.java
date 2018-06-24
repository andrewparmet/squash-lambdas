package com.parmet.squashlambdas.cal;

import com.parmet.squashlambdas.match.Match;

public class ChangeSummaryTest {
  public static ChangeSummary newChangeSummary(Action action, Match match) {
    return new ChangeSummary(action, match);
  }
}
