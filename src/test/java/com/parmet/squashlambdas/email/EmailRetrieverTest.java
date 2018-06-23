package com.parmet.squashlambdas.email;

import static com.google.common.truth.Truth.assertThat;

import biweekly.component.VEvent;
import biweekly.property.Attendee;
import biweekly.property.Organizer;
import com.amazonaws.services.s3.AmazonS3;
import com.parmet.squashlambdas.email.EmailData;
import com.parmet.squashlambdas.email.EmailRetriever;
import com.parmet.squashlambdas.integration.MatchIntegrationTests;
import com.parmet.squashlambdas.testutils.EmailReturningS3;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.sql.Date;
import java.time.Instant;
import java.util.Optional;
import org.junit.Test;

public class EmailRetrieverTest {
  @Test
  public void testRetrieveEmail() {
    AmazonS3 s3 =
        new EmailReturningS3(
            TestUtils.getResourceAsString(MatchIntegrationTests.class, "reservationCreated2"));
    EmailData data = new EmailRetriever(s3, "", "").retrieveEmail();
    assertThat(data).isEqualTo(emailData());
  }

  public static VEvent event() {
    VEvent event = new VEvent();
    event.setDateTimeStamp(Date.from(Instant.parse("2018-03-26T00:27:36Z")));
    event.setUid("741228ef-faa3-4e59-9b1e-ce8b2aefca5a");
    event.setDateStart(Date.from(Instant.parse("2018-03-29T01:00:00Z")));
    event.setDateEnd(Date.from(Instant.parse("2018-03-29T01:45:00Z")));
    event.setSummary("Reservation for Hardball");
    event.setOrganizer(new Organizer("Tennis & Racquet Club", "no-reply@ussquash.com"));
    event.setLocation("Tennis & Racquet Club / Court: Court #7");
    event.addAttendee(attendee());
    return event;
  }

  public static Attendee attendee() {
    Attendee attendee = new Attendee("Andrew Parmet", "andrew@parmet.com");
    attendee.addParameter("CUTYPE", "INDIVIDUAL");
    return attendee;
  }

  public static EmailData emailData() {
    return new EmailData(
        "Tennis & Racquet Club Reservation Confirmation",
        "Hello Andrew Parmet, A reservation including you has been made via the Tennis & Racquet "
            + "Club court reservation system. Reservation details: Court: Court #7 - Hardball "
            + "Date: Wednesday, March 28th 2018 Time: 09:00 PM to 09:45 PM To cancel your spot "
            + "or the whole reservation please log into Club Locker and use the My Reservations "
            + "area.",
        Optional.of(event()));
  }
}
