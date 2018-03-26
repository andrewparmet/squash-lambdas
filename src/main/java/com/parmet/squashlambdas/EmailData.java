package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class EmailData {
  private final String subject;
  private final String body;
  private final Calendar event;

  public EmailData(String subject, String body, Calendar event) {
    this.subject = checkNotNull(subject, "subject");
    this.body = checkNotNull(body, "htmlBody");
    this.event = checkNotNull(event, "event");
    PropertyList<Property> properties = event.getComponent("VEVENT").getProperties();
    DtStart start = properties.getProperty("DTSTART");
    start.getDate().toInstant();
    start.getTimeZone();
    DtEnd end = properties.getProperty("DTEND");
    //properties.getProperty(aName)
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
