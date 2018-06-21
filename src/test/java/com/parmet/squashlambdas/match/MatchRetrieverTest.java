package com.parmet.squashlambdas.match;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetrieverTest;
import com.parmet.squashlambdas.match.Match;
import com.parmet.squashlambdas.match.MatchRetriever;
import com.parmet.squashlambdas.s3.BucketInfo;
import com.parmet.squashlambdas.s3.S3CreateObjectInfo;
import com.parmet.squashlambdas.s3.S3EmailNotification;
import com.parmet.squashlambdas.s3.S3ObjectInfo;
import com.parmet.squashlambdas.testutils.EmailReturningS3;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.time.Instant;
import org.junit.Test;

public class MatchRetrieverTest {
  @Test
  public void fromEmailDataTest() throws Exception {
    EmailData data = EmailRetrieverTest.emailData();
    Match match = MatchRetriever.fromEmailData(data);

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
    assertThat(getMatch(TestUtils.getResourceAsString("fpdc9cule6ne0okl6jrtdcpv2fpaov031jom6n81")))
        .isEqualTo(
            new Match(
                Court.COURT_7,
                ImmutableSet.of(),
                Instant.parse("2018-03-29T01:00:00Z"),
                Instant.parse("2018-03-29T01:45:00Z")));
  }

  public static Match getMatch(String body) {
    return new MatchRetriever(
            new EmailReturningS3(body),
            new S3EmailNotification(
                "2.0",
                "aws:s3",
                "us-east-1",
                Instant.parse("2018-03-26T00:27:50.117Z"),
                "ObjectCreated:Put",
                new S3CreateObjectInfo(
                    new BucketInfo("parmet-squash-emails"),
                    new S3ObjectInfo("emails/some-file-name"))))
        .getMatch();
  }
}
