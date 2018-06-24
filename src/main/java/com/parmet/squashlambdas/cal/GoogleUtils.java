package com.parmet.squashlambdas.cal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.common.collect.ImmutableList;
import com.parmet.squashlambdas.util.Utils;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoogleUtils {
  private static final Logger log = LogManager.getLogger();

  private static final String APPLICATION_NAME = "PARMET_SQUASH_LAMBDAS";
  private static final HttpTransport TRANSPORT =
      Utils.get(GoogleNetHttpTransport::newTrustedTransport);

  public static void giveUserOwnership(Credential serviceAccountCredential) throws IOException {
    newCalendarService(serviceAccountCredential)
        .acl()
        .insert(
            "primary",
            new AclRule()
                .setRole("owner")
                .setScope(new Scope().setType("user").setValue("andrewparmet@gmail.com")))
        .execute();
  }

  public static void printAcl(Credential credential) throws IOException {
    Acl acl = newCalendarService(credential).acl().list("primary").execute();
    for (AclRule rule : acl.getItems()) {
      log.info("{}:{}", rule.getId(), rule.getRole());
    }
  }

  public static Calendar newCalendarService(Credential credential) {
    return new Calendar.Builder(TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  public static Credential getCredentials() throws IOException {
    return GoogleCredential.fromStream(
        GoogleUtils.class.getResourceAsStream("My Project-221f4ef6560c.json"))
      .createScoped(ImmutableList.of(CalendarScopes.CALENDAR));
  }
}
