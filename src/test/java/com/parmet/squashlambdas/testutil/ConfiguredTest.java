package com.parmet.squashlambdas.testutil;

import com.google.api.services.calendar.Calendar;
import com.parmet.squashlambdas.ConfigUtils;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;

public abstract class ConfiguredTest {
  private Configuration config;
  private Calendar calendar;

  protected Calendar calendar() {
    return calendar;
  }

  @Before
  public void before() {
    this.config = ConfigUtils.loadConfiguration("test.xml");
    this.calendar = ConfigUtils.configureCalendar(config, null);
  }

  @After
  public void after() {
  }
}
