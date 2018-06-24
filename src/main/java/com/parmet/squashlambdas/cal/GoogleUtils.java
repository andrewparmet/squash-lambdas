package com.parmet.squashlambdas.cal;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoogleUtils {
  private static final Logger log = LogManager.getLogger();

  public static void giveUserOwnership(Calendar calendar, String userEmail) throws IOException {
    calendar
        .acl()
        .insert(
            "primary",
            new AclRule()
                .setRole("owner")
                .setScope(new Scope().setType("user").setValue(userEmail)))
        .execute();
  }

  public static void printAcl(Calendar calendar) throws IOException {
    Acl acl = calendar.acl().list("primary").execute();
    for (AclRule rule : acl.getItems()) {
      log.info("{}:{}", rule.getId(), rule.getRole());
    }
  }
}
