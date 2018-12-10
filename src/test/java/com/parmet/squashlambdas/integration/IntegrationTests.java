package com.parmet.squashlambdas.integration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.activity.Activity;
import com.parmet.squashlambdas.activity.Clinic;
import com.parmet.squashlambdas.activity.Court;
import com.parmet.squashlambdas.activity.Match;
import com.parmet.squashlambdas.cal.Action;
import com.parmet.squashlambdas.cal.ChangeSummary;
import com.parmet.squashlambdas.cal.ChangeSummaryTest;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.testutil.TestUtils;
import java.time.Instant;
import java.util.Optional;
import org.junit.Test;

public class IntegrationTests {
  @Test
  public void testReservationCreated() {
    assertThat(getSummary("reservationCreated"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_2,
                    Instant.parse("2018-04-17T22:45:00Z"),
                    Instant.parse("2018-04-17T23:30:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testReservationCreated2() {
    assertThat(getSummary("reservationCreated2"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_7,
                    Instant.parse("2018-03-29T01:00:00Z"),
                    Instant.parse("2018-03-29T01:45:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testReservationCreated3() {
    assertThat(getSummary("reservationCreated3"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_2,
                    Instant.parse("2018-03-26T22:45:00Z"),
                    Instant.parse("2018-03-26T23:30:00Z"),
                    ImmutableSet.of("Philipp Rimmler"))));
  }

  @Test
  public void testReservationCreated4() {
    assertThat(getSummary("reservationCreated4"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_1,
                    Instant.parse("2018-07-03T22:45:00Z"),
                    Instant.parse("2018-07-03T23:30:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testReservationJoined() {
    assertThat(getSummary("someoneJoinsMyReservation"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_2,
                    Instant.parse("2018-05-31T23:30:00Z"),
                    Instant.parse("2018-06-01T00:15:00Z"),
                    ImmutableSet.of("Stephen Santulli"))));
  }

  @Test
  public void testReservationJoined2() {
    assertThat(getSummary("someoneJoinsMyReservation2"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_3,
                    Instant.parse("2018-06-29T00:15:00Z"),
                    Instant.parse("2018-06-29T01:00:00Z"),
                    ImmutableSet.of("Aaron bhole (Guest)"))));
  }

  @Test
  public void testReservationJoined3() {
    assertThat(getSummary("someoneJoinsMyReservation3"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_1,
                    Instant.parse("2018-06-12T22:45:00Z"),
                    Instant.parse("2018-06-12T23:30:00Z"),
                    ImmutableSet.of("James Wall"))));
  }

  @Test
  public void testReservationJoined4() {
    assertThat(getSummary("someoneJoinsMyReservation4"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_1,
                    Instant.parse("2018-03-28T22:45:00Z"),
                    Instant.parse("2018-03-28T23:30:00Z"),
                    ImmutableSet.of("Bruce Chafee"))));
  }

  @Test
  public void testRemovedFromReservation() {
    assertThat(getSummary("removedFromReservation"))
        .hasValue(
            summary(
                Action.DELETE,
                new Match(
                    Court.COURT_1,
                    Instant.parse("2018-04-26T22:45:00Z"),
                    Instant.parse("2018-04-26T23:30:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testJoinReservationWithPlayer() {
    assertThat(getSummary("joinReservationWithPlayer"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_3,
                    Instant.parse("2018-06-28T23:30:00Z"),
                    Instant.parse("2018-06-29T00:15:00Z"),
                    ImmutableSet.of("Paul Cathcart"))));
  }

  @Test
  public void testJoinReservationWithPlayer2() {
    assertThat(getSummary("joinReservationWithPlayer2"))
        .hasValue(
            summary(
                Action.CREATE,
                new Match(
                    Court.COURT_3,
                    Instant.parse("2018-04-25T22:00:00Z"),
                    Instant.parse("2018-04-25T22:45:00Z"),
                    ImmutableSet.of("Bruce Chafee"))));
  }

  @Test
  public void testPlayerHasBeenRemoved() {
    assertThat(getSummary("playerHasBeenRemoved"))
        .hasValue(
            summary(
                Action.UPDATE,
                new Match(
                    Court.COURT_3,
                    Instant.parse("2018-03-20T23:30:00Z"),
                    Instant.parse("2018-03-21T00:15:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testReminder() {
    assertThat(getSummary("reminder")).isEmpty();
  }

  @Test
  public void testReconfirm() throws Exception {
    assertThat(getSummary("reconfirmReservation")).isEmpty();
  }

  @Test
  public void testScoreRecordedWin() throws Exception {
    assertThat(getSummary("scoreRecordedWin")).isEmpty();
  }

  @Test
  public void testScoreRecordedLoss() {
    assertThat(getSummary("scoreRecordedLoss")).isEmpty();
  }

  @Test
  public void testReservationReleased() {
    assertThat(getSummary("reservationReleased"))
        .hasValue(
            summary(
                Action.DELETE,
                new Match(
                    Court.COURT_3,
                    Instant.parse("2018-06-29T00:15:00Z"),
                    Instant.parse("2018-06-29T01:00:00Z"),
                    ImmutableSet.of())));
  }

  @Test
  public void testYourReservationHasBeenCancelled() {
    assertThat(getSummary("yourReservationHasBeenCancelled")).isEmpty();
  }

  @Test
  public void testReservationIncludingYouHasBeenCancelled() {
    assertThat(getSummary("reservationIncludingYouHasBeenCancelled"))
        .hasValue(
            summary(
                Action.DELETE,
                new Match(
                    Court.COURT_1,
                    Instant.parse("2018-10-11T23:30:00Z"),
                    Instant.parse("2018-10-12T00:15:00Z"),
                    ImmutableSet.of("Elisabeth Hill"))));
  }
  
  @Test
  public void testActivityJoined() {
    assertThat(getSummary("activityJoined"))
        .hasValue(
            summary(
                Action.CREATE,
                new Clinic(
                    Court.COURT_3,
                    Instant.parse("2018-12-03T23:45:00Z"),
                    Instant.parse("2018-12-04T00:30:00Z"))));
  }

  private static Optional<ChangeSummary> getSummary(String fileName) {
    return ChangeSummary.fromEmail(
        EmailRetrieverTest.fromBody(TestUtils.getResourceAsString(fileName)));
  }

  private static ChangeSummary summary(Action action, Activity activity) {
    return ChangeSummaryTest.newChangeSummary(action, activity);
  }
}
