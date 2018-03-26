package com.parmet.squashlambdas;

import com.amazonaws.services.s3.AmazonS3;
import com.bizo.awsstubs.services.s3.AmazonS3Stub;
import com.parmet.squashlambdas.testutils.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class EmailRetrieverTest {
  private static final Logger log = LogManager.getLogger();

  @Test
  public void testRetrieveEmail() throws Exception {
    AmazonS3 s3 =
        new AmazonS3Stub() {
          @Override
          public String getObjectAsString(String bucket, String key) {
            return TestUtils.getResourceAsString("fpdc9cule6ne0okl6jrtdcpv2fpaov031jom6n81");
          }
        };

    EmailData data =
        new EmailRetriever(s3, EmailHandlerTest.fromFile("s3CreateObject.json")).retrieveEmail();

    log.info(data);
  }
}
