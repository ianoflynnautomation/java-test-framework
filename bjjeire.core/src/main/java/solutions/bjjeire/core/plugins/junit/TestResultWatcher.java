package solutions.bjjeire.core.plugins.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;

import java.util.Optional;

public class TestResultWatcher implements TestWatcher {
    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);

        try {

            PluginExecutionEngine.postAfterTest(JunitBaseTest.CURRENT_TEST_RESULT.get(), JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(), extensionContext.getTestMethod().get(), throwable);
        } catch (Exception e) {
            PluginExecutionEngine.afterTestFailed(e);
        }
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);

        try {
            PluginExecutionEngine.postAfterTest(JunitBaseTest.CURRENT_TEST_RESULT.get(), JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(), extensionContext.getTestMethod().get(), null);
        } catch (Exception e) {
            PluginExecutionEngine.afterTestFailed(e);
        }
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);

        try {
            PluginExecutionEngine.postAfterTest(JunitBaseTest.CURRENT_TEST_RESULT.get(), JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(), extensionContext.getTestMethod().get(), throwable);
        } catch (Exception e) {
            PluginExecutionEngine.afterTestFailed(e);
        }
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);

        try {
            PluginExecutionEngine.postAfterTest(JunitBaseTest.CURRENT_TEST_RESULT.get(), JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(), extensionContext.getTestMethod().get(), null);
        } catch (Exception e) {
            PluginExecutionEngine.afterTestFailed(e);
        }
    }
}