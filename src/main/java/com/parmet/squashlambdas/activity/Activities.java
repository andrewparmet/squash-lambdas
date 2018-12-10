package com.parmet.squashlambdas.activity;

import static com.google.common.base.Preconditions.checkArgument;

import com.parmet.squashlambdas.email.EmailData;

final class Activities {
  static Activity fromEmailData(EmailData email) {
    if (isMatch(email)) {
      return Match.fromEmailData(email);
    } else {
      checkArgument(isClinic(email), "unknown activity type: %s", email);
      return Clinic.fromEmailData(email);
    }
  }
  
  // This is imperfect.
  private static boolean isMatch(EmailData email) {
    return !isClinic(email);
  }
  
  // This seems to be reliable.
  private static boolean isClinic(EmailData email) {
    return email.getBody().contains("Clinic");
  }
}
