package com.parmet.squashlambdas.cal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class CalendarQuickstart {
  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    Credential cred = Utils.getCredentials(transport);
    Utils.printAcl(cred);
  }
}
