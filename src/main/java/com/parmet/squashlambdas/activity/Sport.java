package com.parmet.squashlambdas.activity;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public enum Sport {
  SQUASH,
  HARDBALL,
  TENNIS,
  RACQUETS;

  private final String pretty = UPPER_UNDERSCORE.to(UPPER_CAMEL, name());

  @Override
  public String toString() {
    return pretty;
  }
}
