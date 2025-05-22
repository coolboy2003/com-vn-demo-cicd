package com.bot.slack.cab;

import com.bot.slack.cab.config.GeminiApiConfigTest;
import com.bot.slack.cab.controller.BotControllerTest;
import com.bot.slack.cab.helper.TimeUtilTest;
import com.bot.slack.cab.integration.BotIntegrationTest;
import com.bot.slack.cab.model.ModelTest;
import com.bot.slack.cab.service.BotServiceTest;
import com.bot.slack.cab.service.GerminiServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ApplicationTests.class,
    BotServiceTest.class,
    GerminiServiceTest.class,
    BotControllerTest.class,
    TimeUtilTest.class,
    ModelTest.class,
    GeminiApiConfigTest.class,
    BotIntegrationTest.class
})
public class TestSuite {
    // This class serves as a test suite to run all tests
}
