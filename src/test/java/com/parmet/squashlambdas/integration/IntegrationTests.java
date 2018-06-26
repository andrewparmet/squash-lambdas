package com.parmet.squashlambdas.integration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.cal.Action;
import com.parmet.squashlambdas.cal.ChangeSummary;
import com.parmet.squashlambdas.cal.ChangeSummaryTest;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.match.Court;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.time.Instant;
import java.util.Optional;
import org.junit.Test;

public class IntegrationTests {
  @Test
  public void testReservationCreated() throws Exception {
    assertThat(getSummary("reservationCreated"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_2,
                    ImmutableSet.of(),
                    Instant.parse("2018-04-17T22:45:00Z"),
                    Instant.parse("2018-04-17T23:30:00Z"))));
  }

  @Test
  public void testReservationCreated2() throws Exception {
    assertThat(getSummary("reservationCreated2"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_7,
                    ImmutableSet.of(),
                    Instant.parse("2018-03-29T01:00:00Z"),
                    Instant.parse("2018-03-29T01:45:00Z"))));
  }

  @Test
  public void testReservationCreated3() throws Exception {
    assertThat(getSummary("reservationCreated3"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_2,
                    ImmutableSet.of("Philipp Rimmler"),
                    Instant.parse("2018-03-26T22:45:00Z"),
                    Instant.parse("2018-03-26T23:30:00Z"))));
  }

  @Test
  public void testReservationCreated4() throws Exception {
    assertThat(getSummary("reservationCreated4"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_1,
                    ImmutableSet.of(),
                    Instant.parse("2018-07-03T22:45:00Z"),
                    Instant.parse("2018-07-03T23:30:00Z"))));
  }

  @Test
  public void testReservationJoined() throws Exception {
    assertThat(getSummary("someoneJoinsMyReservation"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_2,
                    ImmutableSet.of("Stephen Santulli"),
                    Instant.parse("2018-05-31T23:30:00Z"),
                    Instant.parse("2018-06-01T00:15:00Z"))));
  }

  @Test
  public void testReservationJoined2() throws Exception {
    assertThat(getSummary("someoneJoinsMyReservation2"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_3,
                    ImmutableSet.of("Aaron bhole (Guest)"),
                    Instant.parse("2018-06-29T00:15:00Z"),
                    Instant.parse("2018-06-29T01:00:00Z"))));
  }

  @Test
  public void testReservationJoined3() throws Exception {
    assertThat(getSummary("someoneJoinsMyReservation3"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_1,
                    ImmutableSet.of("James Wall"),
                    Instant.parse("2018-06-12T22:45:00Z"),
                    Instant.parse("2018-06-12T23:30:00Z"))));
  }

  @Test
  public void testReservationJoined4() throws Exception {
    assertThat(getSummary("someoneJoinsMyReservation4"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_1,
                    ImmutableSet.of("Bruce Chafee"),
                    Instant.parse("2018-03-28T22:45:00Z"),
                    Instant.parse("2018-03-28T23:30:00Z"))));
  }

  @Test
  public void testRemovedFromReservation() throws Exception {
    assertThat(getSummary("removedFromReservation"))
        .hasValue(
            summary(
                Action.DELETE,
                new Match(
                    Court.COURT_1,
                    ImmutableSet.of(),
                    Instant.parse("2018-04-26T22:45:00Z"),
                    Instant.parse("2018-04-26T23:30:00Z"))));
  }

  @Test
  public void testJoinReservationWithPlayer() throws Exception {
    assertThat(getSummary("joinReservationWithPlayer"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_3,
                    ImmutableSet.of("Paul Cathcart"),
                    Instant.parse("2018-06-28T23:30:00Z"),
                    Instant.parse("2018-06-29T00:15:00Z"))));
  }

  @Test
  public void testJoinReservationWithPlayer2() throws Exception {
    assertThat(getSummary("joinReservationWithPlayer2"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_3,
                    ImmutableSet.of("Bruce Chafee"),
                    Instant.parse("2018-04-25T22:00:00Z"),
                    Instant.parse("2018-04-25T22:45:00Z"))));
  }

  @Test
  public void testPlayerHasBeenRemoved() throws Exception {
    assertThat(getSummary("playerHasBeenRemoved"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_3,
                    ImmutableSet.of(),
                    Instant.parse("2018-03-20T23:30:00Z"),
                    Instant.parse("2018-03-21T00:15:00Z"))));
  }

  @Test
  public void testReminder() throws Exception {
    assertThat(getSummary("reminder")).isEmpty();
  }

  @Test
  public void testReservationReleased() throws Exception {
    assertThat(getSummary("reservationReleased"))
        .hasValue(
            summary(
                Action.DELETE,
                new Match(
                    Court.COURT_3,
                    ImmutableSet.of(),
                    Instant.parse("2018-06-29T00:15:00Z"),
                    Instant.parse("2018-06-29T01:00:00Z"))));
  }

  private static Optional<ChangeSummary> getSummary(String fileName) {
    return ChangeSummary.fromEmail(
        EmailRetrieverTest.fromBody(TestUtils.getResourceAsString(fileName)));
  }

  private static ChangeSummary summary(Action action, Match match) {
    return ChangeSummaryTest.newChangeSummary(action, match);
  }
}
