package solutions.bjjeire.cucumber.runner;

import io.cucumber.core.options.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;


/**
 * Main test runner for BDD tests using the JUnit 5 Suite engine.
 * It automatically discovers step definitions and hooks in the specified glue path.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "solutions.bjjeire.cucumber")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/cucumber-report.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = Constants.SNIPPET_TYPE_PROPERTY_NAME, value = "camelcase")
@ConfigurationParameter(key = "cucumber.execution.strict", value = "true")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @Ignore")
public class CucumberTestRunner {

}
