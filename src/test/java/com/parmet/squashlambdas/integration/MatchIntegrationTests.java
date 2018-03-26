package com.parmet.squashlambdas.integration;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.Court;
import com.parmet.squashlambdas.Match;
import com.parmet.squashlambdas.MatchRetrieverTest;
import com.parmet.squashlambdas.Sport;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.time.Instant;
import org.junit.Test;

public class MatchIntegrationTests {
  @Test
  public void testHardballAlone() throws Exception {
    assertThat(getMatch("fpdc9cule6ne0okl6jrtdcpv2fpaov031jom6n81"))
        .isEqualTo(
            new Match(
                Sport.HARDBALL,
                Court.COURT_7,
                ImmutableSet.of(),
                Instant.parse("2018-03-29T01:00:00Z"),
                Instant.parse("2018-03-29T01:45:00Z")));
  }

  @Test
  public void testSquashAloneSomeoneJoins() throws Exception {
    assertThat(getMatch("5lbjm408aptg4dn8k0aahtucqsr6he5vhnvmqko1"))
        .isEqualTo(
            new Match(
                Sport.SQUASH,
                Court.COURT_1,
                ImmutableSet.of(),
                Instant.parse("2018-03-28T22:45:00Z"),
                Instant.parse("2018-03-28T23:30:00Z")));
  }

  private static Match getMatch(String fileName) {
    return MatchRetrieverTest.getMatch(TestUtils.getResourceAsString(fileName));
  }
}
