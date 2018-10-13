package com.parmet.squashlambdas.email;

import static com.google.common.base.Preconditions.checkNotNull;

import biweekly.component.VEvent;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class EmailData {
  private final String subject;
  private final String body;
  private final Optional<VEvent> event;

  public EmailData(String subject, String body, Optional<VEvent> event) {
    this.subject = checkNotNull(subject, "subject");
    this.body = checkNotNull(body, "htmlBody");
    this.event = checkNotNull(event, "event");
  }

  public Optional<VEvent> getEvent() {
    return event;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
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
