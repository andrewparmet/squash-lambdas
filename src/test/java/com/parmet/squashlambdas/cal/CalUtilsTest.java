package com.parmet.squashlambdas.cal;

import com.parmet.squashlambdas.testutil.ConfiguredTest;
import org.junit.Ignore;
import org.junit.Test;

public class CalUtilsTest extends ConfiguredTest {
  @Test
  @Ignore
  public void giveUserOwnership() throws Exception {
    CalUtils.printAcl(calendar(), config().getString("google.calendarId"));
    CalUtils.giveUserOwnership(calendar(), config().getString("google.calendarId"), "");
    CalUtils.printAcl(calendar(), config().getString("google.calendarId"));
  }
}
