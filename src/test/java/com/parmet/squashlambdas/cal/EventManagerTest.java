package com.parmet.squashlambdas.cal;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.activity.Court;
import com.parmet.squashlambdas.activity.Match;
import com.parmet.squashlambdas.testutil.ConfiguredTest;
import java.time.Instant;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // These actually modify a calendar
public class EventManagerTest extends ConfiguredTest {
  @Test
  public void testCreateMatch() throws Exception {
    manager().create(
        new Match(
            Court.COURT_3,
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z"),
            ImmutableSet.of()));
  }

  @Test
  public void testUpdateMatch() throws Exception {
    manager().update(
        new Match(
            Court.COURT_3,
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z"),
            ImmutableSet.of("Logan Ramseyer")));
  }

  @Test
  public void testDeleteMatch() throws Exception {
    manager().delete(
        new Match(
            Court.COURT_3,
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z"),
            ImmutableSet.of("Logan Ramseyer")));
  }

  private EventManager manager() {
    return new EventManager(calendar(), config().getString("google.calendarId"));
  }
}
