package solutions.bjjeire.core.plugins.testng;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;

@Slf4j
@Listeners(TestResultListener.class)
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

  static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
  static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD =
      ThreadLocal.withInitial(TimeRecord::new);
  private static final ThreadLocal<Boolean> CONFIGURATION_EXECUTED =
      ThreadLocal.withInitial(() -> false);

  @BeforeClass(alwaysRun = true)
  public void beforeClass() {
    var testClassName = this.getClass().getSimpleName();
    log.info(
        "Executing @BeforeClass phase", StructuredArguments.keyValue("testClass", testClassName));
    try {
      if (!CONFIGURATION_EXECUTED.get()) {
        configure();
        CONFIGURATION_EXECUTED.set(true);
      }
      PluginExecutionEngine.preBeforeClass(this.getClass());
      beforeAll();
    } catch (Exception e) {
      log.error(
          "Error during @BeforeClass phase",
          StructuredArguments.keyValue("testClass", testClassName),
          e);
    }
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethodCore(ITestResult result) {
    log.info(
        "Executing @BeforeMethod phase",
        StructuredArguments.keyValue("testName", result.getName()));
    try {
      var methodInfo = this.getClass().getMethod(result.getMethod().getMethodName());
      PluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
      beforeEach();
    } catch (Exception e) {
      log.error(
          "Error during @BeforeMethod phase",
          StructuredArguments.keyValue("testName", result.getName()),
          e);
      PluginExecutionEngine.beforeTestFailed(e);
    }
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethodCore(ITestResult result) {
    log.info(
        "Executing @AfterMethod phase", StructuredArguments.keyValue("testName", result.getName()));
    try {
      var methodInfo = this.getClass().getMethod(result.getMethod().getMethodName());
      afterEach();
      PluginExecutionEngine.postAfterTest(
          CURRENT_TEST_RESULT.get(),
          CURRENT_TEST_TIME_RECORD.get(),
          methodInfo,
          result.getThrowable());
    } catch (Exception e) {
      log.error(
          "Error during @AfterMethod phase",
          StructuredArguments.keyValue("testName", result.getName()),
          e);
    }
  }

  @AfterClass(alwaysRun = true)
  public void afterClassCore() {
    var testClassName = this.getClass().getSimpleName();
    log.info(
        "Executing @AfterClass phase", StructuredArguments.keyValue("testClass", testClassName));
    try {
      afterAll();
      PluginExecutionEngine.postAfterClass(this.getClass());
    } catch (Exception e) {
      log.error(
          "Error during @AfterClass phase",
          StructuredArguments.keyValue("testClass", testClassName),
          e);
    }
  }

  protected void configure() {}

  protected void beforeAll() throws Exception {}

  protected void afterAll() throws Exception {}

  protected void beforeEach() throws Exception {}

  protected void afterEach() {}
}
