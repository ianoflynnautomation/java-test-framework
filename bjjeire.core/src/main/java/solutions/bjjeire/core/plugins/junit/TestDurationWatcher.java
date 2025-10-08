package solutions.bjjeire.core.plugins.junit;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestDurationWatcher
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
  @Override
  public void beforeTestExecution(ExtensionContext context) {
    JunitBaseTest.CURRENT_TEST_TIME_RECORD.get().setStartTime(System.currentTimeMillis());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    JunitBaseTest.CURRENT_TEST_TIME_RECORD.get().setEndTime(System.currentTimeMillis());
  }
}
