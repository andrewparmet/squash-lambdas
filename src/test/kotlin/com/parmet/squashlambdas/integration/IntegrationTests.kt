package com.parmet.squashlambdas.integration

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.testutil.EmailReturningS3
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.Test
import java.time.Instant

class IntegrationTests {
    @Test
    fun `reservation created`() {
        assertThat(getSummary("reservationCreated"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court2,
                        Instant.parse("2018-04-17T22:45:00Z"),
                        Instant.parse("2018-04-17T23:30:00Z"),
                        setOf())))
    }

    @Test
    fun `reservation created 2`() {
        assertThat(getSummary("reservationCreated2"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court7,
                        Instant.parse("2018-03-29T01:00:00Z"),
                        Instant.parse("2018-03-29T01:45:00Z"),
                        setOf())))
    }

    @Test
    fun `reservation created 3`() {
        assertThat(getSummary("reservationCreated3"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court2,
                        Instant.parse("2018-03-26T22:45:00Z"),
                        Instant.parse("2018-03-26T23:30:00Z"),
                        playerSet("Philipp Rimmler"))))
    }

    @Test
    fun `reservation created 4`() {
        assertThat(getSummary("reservationCreated4"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court1,
                        Instant.parse("2018-07-03T22:45:00Z"),
                        Instant.parse("2018-07-03T23:30:00Z"),
                        setOf())))
    }

    @Test
    fun `reservation joined`() {
        assertThat(getSummary("someoneJoinsMyReservation"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.Court2,
                        Instant.parse("2018-05-31T23:30:00Z"),
                        Instant.parse("2018-06-01T00:15:00Z"),
                        playerSet("Stephen Santulli"))))
    }

    @Test
    fun `reservation joined 2`() {
        assertThat(getSummary("someoneJoinsMyReservation2"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.Court3,
                        Instant.parse("2018-06-29T00:15:00Z"),
                        Instant.parse("2018-06-29T01:00:00Z"),
                        playerSet("Aaron bhole (Guest)"))))
    }

    @Test
    fun `reservation joined 3`() {
        assertThat(getSummary("someoneJoinsMyReservation3"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.Court1,
                        Instant.parse("2018-06-12T22:45:00Z"),
                        Instant.parse("2018-06-12T23:30:00Z"),
                        playerSet("James Wall"))))
    }

    @Test
    fun `reservation joined 4`() {
        assertThat(getSummary("someoneJoinsMyReservation4"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.Court1,
                        Instant.parse("2018-03-28T22:45:00Z"),
                        Instant.parse("2018-03-28T23:30:00Z"),
                        playerSet("Bruce Chafee"))))
    }

    @Test
    fun `removed from reservation`() {
        assertThat(getSummary("removedFromReservation"))
            .isEqualTo(
                summary(
                    Action.Delete,
                    Match(
                        Court.Court1,
                        Instant.parse("2018-04-26T22:45:00Z"),
                        Instant.parse("2018-04-26T23:30:00Z"),
                        setOf())))
    }

    @Test
    fun `create reservation with another player`() {
        assertThat(getSummary("joinReservationWithPlayer"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court3,
                        Instant.parse("2018-06-28T23:30:00Z"),
                        Instant.parse("2018-06-29T00:15:00Z"),
                        playerSet("Paul Cathcart"))))
    }

    @Test
    fun `create reservation with another player 2`() {
        assertThat(getSummary("joinReservationWithPlayer2"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.Court3,
                        Instant.parse("2018-04-25T22:00:00Z"),
                        Instant.parse("2018-04-25T22:45:00Z"),
                        playerSet("Bruce Chafee"))))
    }

    @Test
    fun `player leaves reservation`() {
        assertThat(getSummary("playerHasBeenRemoved"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.Court3,
                        Instant.parse("2018-03-20T23:30:00Z"),
                        Instant.parse("2018-03-21T00:15:00Z"),
                        setOf())))
    }

    @Test
    fun `reminder email does nothing`() {
        assertThat(getSummary("reminder")).isNull()
    }

    @Test
    fun `player confirmation email does nothing`() {
        assertThat(getSummary("reconfirmReservation")).isNull()
    }

    @Test
    fun `score recording a win does nothing`() {
        assertThat(getSummary("scoreRecordedWin")).isNull()
    }

    @Test
    fun `score recording a loss does nothing`() {
        assertThat(getSummary("scoreRecordedLoss")).isNull()
    }

    @Test
    fun `leaving a reservation`() {
        assertThat(getSummary("reservationReleased"))
            .isEqualTo(
                summary(
                    Action.Delete,
                    Match(
                        Court.Court3,
                        Instant.parse("2018-06-29T00:15:00Z"),
                        Instant.parse("2018-06-29T01:00:00Z"),
                        setOf())))
    }

    @Test
    fun `cancelled reservation does nothing`() {
        assertThat(getSummary("yourReservationHasBeenCancelled")).isNull()
    }

    @Test
    fun `reservation with me cancelled deletes`() {
        assertThat(getSummary("reservationIncludingYouHasBeenCancelled"))
            .isEqualTo(
                summary(
                    Action.Delete,
                    Match(
                        Court.Court1,
                        Instant.parse("2018-10-11T23:30:00Z"),
                        Instant.parse("2018-10-12T00:15:00Z"),
                        playerSet("Elisabeth Hill"))))
    }

    @Test
    fun `joining an activity`() {
        assertThat(getSummary("activityJoined"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Clinic(
                        Court.Court3,
                        Instant.parse("2018-12-03T23:45:00Z"),
                        Instant.parse("2018-12-04T00:30:00Z"))))
    }

    @Test
    fun `creating a tennis reservation`() {
        assertThat(getSummary("createTennisReservation"))
            .isEqualTo(
                summary(
                    Action.Create,
                    Match(
                        Court.TennisCourt,
                        Instant.parse("2019-07-09T23:30:00Z"),
                        Instant.parse("2019-07-10T00:30:00Z"),
                        setOf())))
    }

    @Test
    fun `player joining a tennis reservation`() {
        assertThat(getSummary("playerJoinsTennisReservation"))
            .isEqualTo(
                summary(
                    Action.Update,
                    Match(
                        Court.TennisCourt,
                        Instant.parse("2019-07-09T23:30:00Z"),
                        Instant.parse("2019-07-10T00:30:00Z"),
                        playerSet("Brayden Minahan"))))
    }

    private fun getSummary(fileName: String) =
        ChangeSummary.fromEmail(emailFromBody(fileName))

    private fun summary(action: Action, activity: Activity) =
        ChangeSummary(action, activity)

    private fun playerSet(name: String) = setOf(Player(name = name))
}

fun emailFromBody(fileName: String) =
    EmailRetriever(EmailReturningS3(getResourceAsString(fileName)))
        .retrieveEmail("parmet-squash-emails", "emails/some-file-name")
