package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import biweekly.component.VEvent;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class EmailData {
  private final String subject;
  private final String body;
  private final VEvent event;

  public EmailData(String subject, String body, VEvent event) {
    this.subject = checkNotNull(subject, "subject");
    this.body = checkNotNull(body, "htmlBody");
    this.event = checkNotNull(event, "event");
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("subject", subject)
        .append("body", body)
        .append("event", event)
        .build();
  }
}
