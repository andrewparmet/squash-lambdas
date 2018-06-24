package com.parmet.squashlambdas.cal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utils {
  private static final Logger log = LogManager.getLogger();

  private static final String APPLICATION_NAME = "PARMET_SQUASH_LAMBDAS";

  public static void giveUserOwnership(Credential serviceAccountCredential) throws IOException {
    newCalendarService(serviceAccountCredential)
        .acl()
        .insert(
            "primary",
            new AclRule().setRole("owner")
                .setScope(
                    new Scope().setType("user").setValue("andrewparmet@gmail.com")))
        .execute();
  }

  public static void printAcl(Credential credential) throws IOException {
    Acl acl = newCalendarService(credential).acl().list("primary").execute();
    for (AclRule rule : acl.getItems()) {
      log.info("{}:{}", rule.getId(), rule.getRole());
    }
  }

  public static Calendar newCalendarService(Credential credential) {
    try {
      NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      return new Calendar.Builder(transport, JacksonFactory.getDefaultInstance(), credential)
          .setApplicationName(APPLICATION_NAME)
          .build();
    } catch (IOException | GeneralSecurityException ex) {
      throw new UncheckedExecutionException(ex);
    }
  }

  public static Credential getCredentials(NetHttpTransport transport) throws IOException {
    return GoogleCredential.fromStream(
        Utils.class.getResourceAsStream("My Project-221f4ef6560c.json"))
      .createScoped(ImmutableList.of(CalendarScopes.CALENDAR));
  }
}
