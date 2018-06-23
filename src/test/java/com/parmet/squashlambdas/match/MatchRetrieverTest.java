package com.parmet.squashlambdas.match;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetriever;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.integration.MatchIntegrationTests;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.match.MatchRetriever;
import com.parmet.squashlambdas.testutils.EmailReturningS3;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.time.Instant;
import org.junit.Test;

public class MatchRetrieverTest {
  @Test
  public void fromEmailDataTest() throws Exception {
    EmailData data = EmailRetrieverTest.emailData();
    Match match = Match.getFromEmailData(data);

    Match expected =
        new Match(
            Court.COURT_7,
            ImmutableSet.of(),
            Instant.parse("2018-03-29T01:00:00Z"),
            Instant.parse("2018-03-29T01:45:00Z"));
    assertThat(match).isEqualTo(expected);
  }

  @Test
  public void testHardballAlone() throws Exception {
    assertThat(
        getMatch(
            TestUtils.getResourceAsString(MatchIntegrationTests.class, "reservationCreated2")))
        .isEqualTo(
            new Match(
                Court.COURT_7,
                ImmutableSet.of(),
                Instant.parse("2018-03-29T01:00:00Z"),
                Instant.parse("2018-03-29T01:45:00Z")));
  }

  public static Match getMatch(String body) {
    return new MatchRetriever(
        new EmailRetriever(
            new EmailReturningS3(body),
            "parmet-squash-emails",
            "emails/some-file-name").retrieveEmail())
        .getMatch();
  }
}
