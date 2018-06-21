package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkNotNull;

import biweekly.component.VEvent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

  public VEvent getEvent() {
    return event;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    EmailData rhs = (EmailData) obj;
    return new EqualsBuilder()
        .append(subject, rhs.subject)
        .append(body, rhs.body)
        .append(event, rhs.event)
        .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(subject)
        .append(body)
        .append(event)
        .build();
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
