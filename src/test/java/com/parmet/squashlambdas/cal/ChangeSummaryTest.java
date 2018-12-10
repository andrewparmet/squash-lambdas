package com.parmet.squashlambdas.cal;

import com.parmet.squashlambdas.activity.Activity;

public class ChangeSummaryTest {
  public static ChangeSummary newChangeSummary(Action action, Activity activity) {
    return new ChangeSummary(action, activity);
  }
}
