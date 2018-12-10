package com.parmet.squashlambdas.activity;

import static com.google.common.truth.Truth.assertThat;

import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.integration.IntegrationTests;
import com.parmet.squashlambdas.testutil.TestUtils;
import java.time.Instant;
import org.junit.Test;

public class ActivityParserTest {
  @Test
  public void fromEmailDataTest() throws Exception {
    EmailData data = EmailRetrieverTest.emailData();
    ActivityParser parser = new ActivityParser(data);

    assertThat(parser.parseCourt()).isEqualTo(Court.COURT_7);
    assertThat(parser.parseStartAndEnd().getStart())
        .isEqualTo(Instant.parse("2018-03-29T01:00:00Z"));
    assertThat(parser.parseStartAndEnd().getEnd())
        .isEqualTo(Instant.parse("2018-03-29T01:45:00Z"));
    assertThat(parser.parseOtherPlayers()).isEmpty();
  }

  @Test
  public void testHardballAlone() throws Exception {
    ActivityParser parser =
        new ActivityParser(
            EmailRetrieverTest.fromBody(
                TestUtils.getResourceAsString(
                    IntegrationTests.class,
                    "reservationCreated2")));

    assertThat(parser.parseCourt()).isEqualTo(Court.COURT_7);
    assertThat(parser.parseStartAndEnd().getStart())
        .isEqualTo(Instant.parse("2018-03-29T01:00:00Z"));
    assertThat(parser.parseStartAndEnd().getEnd())
        .isEqualTo(Instant.parse("2018-03-29T01:45:00Z"));
    assertThat(parser.parseOtherPlayers()).isEmpty();
  }
}
