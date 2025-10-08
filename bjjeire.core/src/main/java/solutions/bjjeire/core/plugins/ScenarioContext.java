package solutions.bjjeire.core.plugins;

public class ScenarioContext {
  private final String scenarioName;
  private final TestResult testResult;
  private final BrowserConfiguration browserConfiguration;

  public ScenarioContext(
      String scenarioName, TestResult testResult, BrowserConfiguration browserConfiguration) {
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
