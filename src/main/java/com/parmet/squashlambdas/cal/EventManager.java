package com.parmet.squashlambdas.cal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.common.collect.Iterables;
import com.parmet.squashlambdas.activity.Activity;
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

  public void create(Activity activity) throws IOException {
    log.info("Creating schdulable {}", activity);
    calendar.events().insert(calendarId, activity.toEvent()).execute();
  }

  public void update(Activity activity) throws IOException {
    log.info("Updating activity {}", activity);

    List<Event> events = findEvents(activity);

    if (events.size() > 1) {
      log.info("Found too many events to update; deleting them all. {}", events);
      for (Event event : events) {
        calendar.events().delete(calendarId, event.getId()).execute();
      }
      create(activity);
    } else {
      Event event = Iterables.getOnlyElement(events);
      log.info("Found one event to update: {}", event);
      calendar.events().patch(calendarId, event.getId(), activity.toEvent()).execute();
    }
  }

  public void delete(Activity activity) throws IOException {
    log.info("Deleting activity {}", activity);
    for (Event event : findEvents(activity)) {
      log.info("Deleting event {}", event);
      calendar.events().delete(calendarId, event.getId()).execute();
    }
  }

  private List<Event> findEvents(Activity activity) throws IOException {
    return calendar.events().list(calendarId)
        .setQ(activity.searchString())
        .execute()
        .getItems();
  }
}
