package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import biweekly.component.VEvent;
import biweekly.property.Attendee;
import com.amazonaws.services.s3.AmazonS3;
import java.util.List;
import java.util.Set;

public class MatchRetriever {
  private final AmazonS3 s3;
  private final S3EmailNotification note;

  public MatchRetriever(AmazonS3 s3, S3EmailNotification note) {
    this.s3 = checkNotNull(s3, "s3");
    this.note = checkNotNull(note, "note");
  }

  public Match getMatch() {
    return fromEmailData(
        new EmailRetriever(
                s3, note.getS3ObjectInfo().getBucketName(), note.getS3ObjectInfo().getObjectKey())
            .retrieveEmail());
  }

  static Match fromEmailData(EmailData data) {
    VEvent event = data.getEvent();
    Court court = Court.fromLocationString(event.getLocation().getValue());
    return new Match(
        court.getSport(),
        court,
        notMe(event.getAttendees()),
        event.getDateStart().getValue().toInstant(),
        event.getDateEnd().getValue().toInstant());
  }

  static Set<String> notMe(List<Attendee> attendees) {
    return attendees
        .stream()
        .map(Attendee::getCommonName)
        .filter(not("Andrew Parmet"::equals))
        .collect(toImmutableSet());
  }
}
