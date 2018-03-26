package com.parmet.squashlambdas;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parmet.squashlambdas.testutils.TestUtils;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class EmailHandlerTest {
  private static final Logger log = LogManager.getLogger();
  private static final Gson GSON = new Gson();

  @Test
  public void test() throws Exception {
    log.info(fromFile("s3CreateObject.json"));
  }

  public static S3EmailNotification fromFile(String filename) {
    Object obj =
        GSON.fromJson(
            TestUtils.getJsonResourceAsString(filename),
            new TypeToken<Map<String, Object>>() {}.getType());

    return EmailNotificationHandler.fromInputObject(obj);
  }
}
