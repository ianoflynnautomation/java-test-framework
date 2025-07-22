package solutions.bjjeire.core.plugins.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;

import java.lang.reflect.Method;
import java.util.Optional;

public class TestResultWatcher implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(TestResultWatcher.class);

    @Override
    public void testAborted(ExtensionContext context, Throwable throwable) {
        log.warn("Test Aborted: {}", context.getDisplayName(), throwable);
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
        executePostTestActions(context, throwable);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        log.info("Test Disabled: {} - Reason: {}", context.getDisplayName(), reason.orElse("No reason provided"));
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
        executePostTestActions(context, null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable throwable) {
        log.error("Test Failed: {}", context.getDisplayName(), throwable);
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
        executePostTestActions(context, throwable);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.info("Test Successful: {}", context.getDisplayName());
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
        executePostTestActions(context, null);
    }

    private void executePostTestActions(ExtensionContext context, Throwable throwable) {
        PluginExecutionEngine engine = getPluginExecutionEngine(context);
        if (engine == null) {
            log.error("Could not retrieve PluginExecutionEngine from Spring Context. Post-test actions will not run.");
            return;
        }

        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isEmpty()) {
            log.warn("Test method not available in ExtensionContext. Cannot run post-test plugin actions for '{}'.", context.getDisplayName());
            return;
        }

        try {
            engine.postAfterTest(
                    JunitBaseTest.CURRENT_TEST_RESULT.get(),
                    JunitBaseTest.CURRENT_TEST_TIME_RECORD.get(),
                    testMethod.get(),
                    throwable
            );
        } catch (Exception e) {
            log.error("An exception occurred within the postAfterTest plugin execution for test: {}", context.getDisplayName(), e);
            engine.afterTestFailed(e);
        }
    }

    /**
     * Retrieves the PluginExecutionEngine bean from the Spring ApplicationContext.
     */
    private PluginExecutionEngine getPluginExecutionEngine(ExtensionContext context) {
        try {
            ApplicationContext springContext = SpringExtension.getApplicationContext(context);
            return springContext.getBean(PluginExecutionEngine.class);
        } catch (Exception e) {
            log.error("Failed to get ApplicationContext or PluginExecutionEngine bean for context '{}'. " +
                            "Ensure your test class is annotated with @ExtendWith(SpringExtension.class).",
                    context.getDisplayName(), e);
            return null;
        }
    }
}
