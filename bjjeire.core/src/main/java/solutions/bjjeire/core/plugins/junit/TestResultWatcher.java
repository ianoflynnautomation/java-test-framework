package solutions.bjjeire.core.plugins.junit;

import java.lang.reflect.Method;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;

@Slf4j
public class TestResultWatcher implements TestWatcher {

  private static final String EVENT_NAME = "eventName";
  private static final String TEST_CLASS = "testClass";
  private static final String TEST_METHOD = "testMethod";
  private static final String STATUS = "status";
  private static final String REASON = "reason";

  @Override
  public void testAborted(ExtensionContext context, Throwable throwable) {
    JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
    log.warn(
        "Test execution finished",
        StructuredArguments.keyValue(EVENT_NAME, "testExecutionFinished"),
        StructuredArguments.keyValue(TEST_CLASS, context.getRequiredTestClass().getSimpleName()),
        StructuredArguments.keyValue(TEST_METHOD, context.getRequiredTestMethod().getName()),
        StructuredArguments.keyValue(STATUS, "ABORTED"),
        throwable);
    executePostTestActions(context, throwable);
  }

  @Override
  public void testDisabled(ExtensionContext context, Optional<String> reason) {
    JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
    log.info(
        "Test execution finished",
        StructuredArguments.keyValue(EVENT_NAME, "testExecutionFinished"),
        StructuredArguments.keyValue(TEST_CLASS, context.getRequiredTestClass().getSimpleName()),
        StructuredArguments.keyValue(TEST_METHOD, context.getRequiredTestMethod().getName()),
        StructuredArguments.keyValue(STATUS, "DISABLED"),
        StructuredArguments.keyValue(REASON, reason.orElse("No reason provided")));
    executePostTestActions(context, null);
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable throwable) {
    JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
    log.error(
        "Test execution finished",
        StructuredArguments.keyValue(EVENT_NAME, "testExecutionFinished"),
        StructuredArguments.keyValue(TEST_CLASS, context.getRequiredTestClass().getSimpleName()),
        StructuredArguments.keyValue(TEST_METHOD, context.getRequiredTestMethod().getName()),
        StructuredArguments.keyValue(STATUS, "FAILURE"),
        throwable);
    executePostTestActions(context, throwable);
  }

  @Override
  public void testSuccessful(ExtensionContext context) {
    JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
    log.info(
        "Test execution finished",
        StructuredArguments.keyValue(EVENT_NAME, "testExecutionFinished"),
        StructuredArguments.keyValue(TEST_CLASS, context.getRequiredTestClass().getSimpleName()),
        StructuredArguments.keyValue(TEST_METHOD, context.getRequiredTestMethod().getName()),
        StructuredArguments.keyValue(STATUS, "SUCCESS"));
    executePostTestActions(context, null);
  }

  private void executePostTestActions(ExtensionContext context, Throwable throwable) {
    Optional<Method> testMethod = context.getTestMethod();
    if (testMethod.isEmpty()) {
      log.warn(
          "Cannot run post-test plugin actions",
          StructuredArguments.keyValue(EVENT_NAME, "pluginExecutionSkipped"),
          StructuredArguments.keyValue(TEST_METHOD, context.getDisplayName()),
          StructuredArguments.keyValue(REASON, "Test method not available in ExtensionContext"));
      return;
    }

    try {
      PluginExecutionEngine.postAfterTest(
          JunitBaseTest.CURRENT_TEST_RESULT.get(),
          JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(),
          testMethod.get(),
          throwable);
    } catch (Exception e) {
      log.error(
          "An exception occurred within the postAfterTest plugin execution",
          StructuredArguments.keyValue(EVENT_NAME, "pluginExecutionFailed"),
          StructuredArguments.keyValue(TEST_METHOD, context.getDisplayName()),
          e);
    }
  }

  private PluginExecutionEngine getPluginExecutionEngine(ExtensionContext context) {
    try {
      ApplicationContext springContext = SpringExtension.getApplicationContext(context);
      return springContext.getBean(PluginExecutionEngine.class);
    } catch (Exception e) {
      log.error(
          "Failed to get ApplicationContext or PluginExecutionEngine bean",
          StructuredArguments.keyValue(EVENT_NAME, "springContextUnavailable"),
          StructuredArguments.keyValue(TEST_METHOD, context.getDisplayName()),
          StructuredArguments.keyValue(
              "hint", "Ensure test class is annotated with @ExtendWith(SpringExtension.class)."),
          e);
      return null;
    }
  }
}
