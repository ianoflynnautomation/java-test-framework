package solutions.bjjeire.cucumber.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OBJECT_FACTORY_PROPERTY_NAME;

/**
 * This is the main test runner class for BDD tests. It uses the JUnit 5 Suite engine to run Cucumber.
 * With the cucumber-spring dependency, Cucumber will automatically find the @CucumberContextConfiguration
 * on the step definition classes and use Spring for dependency injection.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "solutions.bjjeire.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/cucumber-report.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = SNIPPET_TYPE_PROPERTY_NAME, value = "camelcase")
@ConfigurationParameter(key = "cucumber.execution.strict", value = "true")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @Ignore")
public class CucumberTestRunner {
    // This class remains empty. It's used for configuration only.
    // To run your tests, you can now right-click this file and select "Run CucumberTestRunner".
}