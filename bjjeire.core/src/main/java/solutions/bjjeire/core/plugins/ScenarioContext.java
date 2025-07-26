package solutions.bjjeire.selenium.web.infrastructure;

import solutions.bjjeire.core.plugins.TestResult;

/**
 * A context object that holds information about the currently executing Cucumber Scenario.
 * This is passed to plugins to provide them with the necessary context to perform their actions.
 */
public class ScenarioContext {
    private final String scenarioName;
    private final TestResult testResult;
    private final BrowserConfiguration browserConfiguration;

    public ScenarioContext(String scenarioName, TestResult testResult, BrowserConfiguration browserConfiguration) {
        this.scenarioName = scenarioName;
        this.testResult = testResult;
        this.browserConfiguration = browserConfiguration;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public BrowserConfiguration getBrowserConfiguration() {
        return browserConfiguration;
    }
}