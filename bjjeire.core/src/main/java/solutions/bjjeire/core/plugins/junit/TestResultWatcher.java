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

    private static final Logger logger = LoggerFactory.getLogger(TestResultWatcher.class);

    @Override
    public void testAborted(ExtensionContext context, Throwable throwable) {
        logger.warn("Test was aborted: {}", context.getDisplayName(), throwable);
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
        executePostTestActions(context, throwable);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        logger.info("Test was disabled: {} - Reason: {}", context.getDisplayName(), reason.orElse("No reason provided"));
        // A disabled test is not a failure or success in the execution sense,
        // but for plugin purposes, we can treat it as a success with no throwable.
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
        executePostTestActions(context, null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable throwable) {
        logger.error("Test failed: {}", context.getDisplayName(), throwable);
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
        executePostTestActions(context, throwable);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("Test was successful: {}", context.getDisplayName());
        JunitBaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
        executePostTestActions(context, null);
    }

    private void executePostTestActions(ExtensionContext context, Throwable throwable) {
        // Since this extension is not a Spring bean, we fetch the engine from the context.
        PluginExecutionEngine engine = getPluginExecutionEngine(context);
        if (engine == null) {
            logger.error("Could not retrieve PluginExecutionEngine from Spring Context. Post-test actions will not run.");
            return;
        }

        // It's possible the test method might not be present if the failure happened
        // during class setup. We should handle this gracefully.
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isEmpty()) {
            logger.warn("Test method not available in ExtensionContext. Cannot run post-test actions.");
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
            logger.error("An exception occurred within the postAfterTest plugin execution.", e);
            engine.afterTestFailed(e);
        }
    }

    /**
     * Retrieves the PluginExecutionEngine bean from the Spring ApplicationContext.
     * The SpringExtension makes the ApplicationContext available within the JUnit ExtensionContext.
     *
     * @param context The current ExtensionContext.
     * @return The PluginExecutionEngine instance, or null if it cannot be found.
     */
    private PluginExecutionEngine getPluginExecutionEngine(ExtensionContext context) {
        try {
            ApplicationContext springContext = SpringExtension.getApplicationContext(context);
            return springContext.getBean(PluginExecutionEngine.class);
        } catch (Exception e) {
            logger.error("Failed to get ApplicationContext or PluginExecutionEngine bean for context '{}'. " +
                            "Ensure your test class is annotated with @ExtendWith(SpringExtension.class).",
                    context.getDisplayName(), e);
            return null;
        }
    }
}