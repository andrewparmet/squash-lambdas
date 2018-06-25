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
    new EventManager(calendar()).create(
        new Match(
            Court.COURT_3,
            ImmutableSet.of(),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }

  @Test
  public void testUpdateMatch() throws Exception {
    new EventManager(calendar()).update(
        new Match(
            Court.COURT_3,
            ImmutableSet.of("Logan Ramseyer"),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }

  @Test
  public void testDeleteMatch() throws Exception {
    new EventManager(calendar()).delete(
        new Match(
            Court.COURT_3,
            ImmutableSet.of("Logan Ramseyer"),
            Instant.parse("2018-06-25T23:30:00Z"),
            Instant.parse("2018-06-26T00:15:00Z")));
  }
}
