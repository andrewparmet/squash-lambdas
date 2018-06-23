package com.parmet.squashlambdas.integration;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.match.Court;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.match.MatchRetrieverTest;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.time.Instant;
import org.junit.Test;

public class MatchIntegrationTests {
  @Test
  public void testReservationCreated() throws Exception {
    assertThat(getMatch("reservationCreated"))
        .isEqualTo(
            new Match(
                Court.COURT_2,
                ImmutableSet.of(),
                Instant.parse("2018-04-17T22:45:00Z"),
                Instant.parse("2018-04-17T23:30:00Z")));
  }

  @Test
  public void testReservationCreated2() throws Exception {
    assertThat(getMatch("reservationCreated2"))
        .isEqualTo(
            new Match(
                Court.COURT_7,
                ImmutableSet.of(),
                Instant.parse("2018-03-29T01:00:00Z"),
                Instant.parse("2018-03-29T01:45:00Z")));
  }

  @Test
  public void testReservationJoined() throws Exception {
    assertThat(getMatch("someoneJoinsMyReservation"))
        .isEqualTo(
            new Match(
                Court.COURT_2,
                ImmutableSet.of("Stephen Santulli"),
                Instant.parse("2018-05-28T23:30:00Z"),
                Instant.parse("2018-06-01T00:15:00Z")));
  }

  @Test
  public void testReservationJoined2() throws Exception {
    assertThat(getMatch("someoneJoinsMyReservation2"))
        .isEqualTo(
            new Match(
                Court.COURT_3,
                ImmutableSet.of("Aaron bhole (Guest)"),
                Instant.parse("2018-06-29T00:15:00Z"),
                Instant.parse("2018-03-29T01:00:00Z")));
  }

  @Test
  public void testReservationJoined3() throws Exception {
    assertThat(getMatch("someoneJoinsMyReservation3"))
        .isEqualTo(
            new Match(
                Court.COURT_1,
                ImmutableSet.of("James Wall"),
                Instant.parse("2018-06-28T22:45:00Z"),
                Instant.parse("2018-06-28T23:30:00Z")));
  }

  @Test
  public void testReservationJoined4() throws Exception {
    assertThat(getMatch("someoneJoinsMyReservation4"))
        .isEqualTo(
            new Match(
                Court.COURT_1,
                ImmutableSet.of("Bruce Chafee"),
                Instant.parse("2018-03-28T22:45:00Z"),
                Instant.parse("2018-03-28T23:30:00Z")));
  }

  @Test
  public void testRemovedFromReservation() throws Exception {
    assertThat(getMatch("removedFromReservation"))
        .isEqualTo(
            new Match(
                Court.COURT_1,
                ImmutableSet.of(),
                Instant.parse("2018-04-29T22:45:00Z"),
                Instant.parse("2018-04-29T23:30:00Z")));
  }

  @Test
  public void testJoinReservationWithPlayer() throws Exception {
    assertThat(getMatch("joinReservationWithPlayer"))
        .isEqualTo(
            new Match(
                Court.COURT_3,
                ImmutableSet.of("Paul Cathcart"),
                Instant.parse("2018-06-28T23:30:00Z"),
                Instant.parse("2018-06-29T00:15:00Z")));
  }

  @Test
  public void testJoinReservationWithPlayer2() throws Exception {
    assertThat(getMatch("joinReservationWithPlayer2"))
        .isEqualTo(
            new Match(
                Court.COURT_3,
                ImmutableSet.of("Bruce Chafee"),
                Instant.parse("2018-04-25T22:00:00Z"),
                Instant.parse("2018-04-25T22:45:00Z")));
  }

  private static Match getMatch(String fileName) {
    return MatchRetrieverTest.getMatch(TestUtils.getResourceAsString(fileName));
  }
}
