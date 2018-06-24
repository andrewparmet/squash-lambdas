package com.parmet.squashlambdas.match;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.integration.IntegrationTests;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.match.MatchRetriever;
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
            TestUtils.getResourceAsString(IntegrationTests.class, "reservationCreated2")))
        .isEqualTo(
            new Match(
                Court.COURT_7,
                ImmutableSet.of(),
                Instant.parse("2018-03-29T01:00:00Z"),
                Instant.parse("2018-03-29T01:45:00Z")));
  }

  public static Match getMatch(String body) {
    return new MatchRetriever(EmailRetrieverTest.fromBody(body)).getMatch();
  }
}
