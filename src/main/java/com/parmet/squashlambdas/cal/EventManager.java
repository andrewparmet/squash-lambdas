package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.common.collect.Iterables;
import com.parmet.squashlambdas.match.Match;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventManager {
  private static final Logger log = LogManager.getLogger();

  private final Calendar calendar;
  private final String calendarId;

  public EventManager(Calendar calendar, String calendarId) {
    this.calendar = checkNotNull(calendar, "calendar");
    this.calendarId = checkNotNull(calendarId, "calendarId");
  }

  public void create(Match match) throws IOException {
    log.info("Creating match {}", match);
    calendar.events().insert(calendarId, match.toEvent()).execute();
  }

  public void update(Match match) throws IOException {
    log.info("Updating match {}", match);

    List<Event> events = findEvents(match);

    if (events.size() > 1) {
      log.info("Found too many events to update; deleting them all. {}", events);
      for (Event event : events) {
        calendar.events().delete(calendarId, event.getId()).execute();
      }
      create(match);
    } else {
      Event event = Iterables.getOnlyElement(events);
      log.info("Found one event to update: {}", event);
      calendar.events().patch(calendarId, event.getId(), match.toEvent()).execute();
    }
  }

  public void delete(Match match) throws IOException {
    log.info("Deleting match {}", match);
    for (Event event : findEvents(match)) {
      log.info("Deleting event {}", event);
      calendar.events().delete(calendarId, event.getId()).execute();
    }
  }

  private List<Event> findEvents(Match match) throws IOException {
    return calendar.events().list(calendarId)
        .setQ(match.getStart() + " " + match.getEnd())
        .execute()
        .getItems();
  }
}
