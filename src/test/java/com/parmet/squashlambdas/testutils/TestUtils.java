package com.parmet.squashlambdas.testutils;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.diffplug.common.base.Errors;
import com.google.common.io.CharStreams;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestUtils {
  public static final JsonParser PARSER = new JsonParser();

  public static String removeJsonWhitespace(String string) {
    return PARSER.parse(string).toString();
  }

  public static String getJsonResourceAsString(String resourceName) {
    return removeJsonWhitespace(getResourceAsString(getCallerCallerClass(), resourceName));
  }

  public static String getResourceAsString(String resourceName) {
    return getResourceAsString(getCallerCallerClass(), resourceName);
  }

  public static String getResourceAsString(Class<?> owner, String resourceName) {
    return Errors.rethrow()
        .wrap(() -> {
          try (InputStream is = owner.getResourceAsStream(resourceName);
              InputStreamReader isr = new InputStreamReader(is, UTF_8)) {
            return CharStreams.toString(isr);
          }
        })
        .get();
  }

  private static Class<?> getCallerCallerClass() {
    StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
    StackTraceElement ste = stElements[3];
    ste.getClassName();
    return Errors.rethrow().wrap(() -> Class.forName(ste.getClassName())).get();
  }
}
