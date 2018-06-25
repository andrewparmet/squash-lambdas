package com.parmet.squashlambdas.cal;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.match.Court;
import com.parmet.squashlambdas.match.Match;
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
            ImmutableSet.of(),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }

  @Test
  public void testUpdateMatch() throws Exception {
    manager().update(
        new Match(
            Court.COURT_3,
            ImmutableSet.of("Logan Ramseyer"),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }

  @Test
  public void testDeleteMatch() throws Exception {
    manager().delete(
        new Match(
            Court.COURT_3,
            ImmutableSet.of("Logan Ramseyer"),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }

  private EventManager manager() {
    return new EventManager(calendar(), config().getString("google.calendarId"));
  }
}
