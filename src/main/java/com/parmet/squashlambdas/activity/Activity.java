package com.parmet.squashlambdas.activity;

import com.google.api.services.calendar.model.Event;
import com.parmet.squashlambdas.email.EmailData;

public interface Activity {
  Event toEvent();

  String searchString();
  
  static Activity fromEmailData(EmailData email) {
    return Activities.fromEmailData(email);
  }
}
