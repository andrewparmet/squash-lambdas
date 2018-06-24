package com.parmet.squashlambdas.cal;

import com.google.api.client.auth.oauth2.Credential;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class CalendarQuickstart {
  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    Credential cred = GoogleUtils.getCredentials();
    GoogleUtils.printAcl(cred);
  }
}
