package com.parmet.squashlambdas.activity;

import com.parmet.squashlambdas.activity.TimeParser.StartAndEnd;
import com.parmet.squashlambdas.email.EmailData;
import java.time.Instant;

public final class Clinic extends AbstractActivity {
  public Clinic(Court court, Instant start, Instant end) {
    super(court, start, end);
  }

  public static Clinic fromEmailData(EmailData email) {
    ActivityParser retriever = new ActivityParser(email);
    StartAndEnd startAndEnd = retriever.parseStartAndEnd();
    return new Clinic(
        retriever.parseCourt(),
        startAndEnd.getStart(),
        startAndEnd.getEnd());
  }

  @Override
  protected String summary() {
    return getCourt().getSport() + " Clinic";
  }
}
