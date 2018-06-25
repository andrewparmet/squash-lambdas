package com.parmet.squashlambdas;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.common.collect.ImmutableList;
import com.parmet.squashlambdas.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ConfigUtils {
  public static Configuration loadConfiguration(String file) {
    try {
      ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
      return new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
          .configure(
              new Parameters()
              .xml()
              .setURL(ConfigUtils.class.getResource(file))
              .setValidating(false)
              .setThrowExceptionOnMissing(true))
          .getConfiguration();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static AmazonS3 configureS3() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  public static AmazonSNS configureSns() {
    return AmazonSNSClientBuilder.defaultClient();
  }

  public static Calendar configureCalendar(Configuration config, AmazonS3 s3) {
    Credential cred = Utils.get(() -> loadCredential(config, s3))
        .createScoped(ImmutableList.of(CalendarScopes.CALENDAR));
    return new Calendar.Builder(
        Utils.get(GoogleNetHttpTransport::newTrustedTransport),
        JacksonFactory.getDefaultInstance(),
        cred)
      .setApplicationName("PARMET_SQUASH_LAMBDAS")
      .build();
  }

  private static GoogleCredential loadCredential(Configuration config, AmazonS3 s3)
      throws IOException {
    String credsLocation = config.getString("google.credsLocation");
    if (credsLocation.equals("local")) {
      return GoogleCredential.fromStream(
          Files.newInputStream(Paths.get(config.getString("google.credsFileName"))));
    }
    checkState(credsLocation.equals("s3"));
    return GoogleCredential.fromStream(
        new ByteArrayInputStream(
            s3.getObjectAsString(
                config.getString("aws.googleCalCreds.bucket"),
                config.getString("aws.googleCalCreds.key")).getBytes(UTF_8)));
  }
}
