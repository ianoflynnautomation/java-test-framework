package solutions.bjjeire.cucumber.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "solutions.bjjeire.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/cucumber-report.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @Ignore")
// --- NEW: Configuration for Parallel Execution ---

//// 1. Enable parallel execution
//@ConfigurationParameter(key = CUCUMBER_EXECUTION_PARALLEL_ENABLED_PROPERTY_NAME, value = "true")
//
//// 2. Set the execution strategy.
////    'dynamic' - runs features in parallel.
////    'fixed' - runs scenarios in parallel.
//@ConfigurationParameter(key = CUCUMBER_EXECUTION_PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, value = "dynamic")
//
//// 3. (Optional) If using 'fixed' strategy, specify the number of threads.
//// @ConfigurationParameter(key = CUCUMBER_EXECUTION_PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, value = "4")


public class CucumberTestRunner {

}