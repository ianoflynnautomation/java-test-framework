package solutions.bjjeire.core.plugins.junit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;
import solutions.bjjeire.core.plugins.UsesPlugins;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({TestResultWatcher.class, TestDurationWatcher.class})
public abstract class JunitBaseTest extends UsesPlugins {

    private static final Logger log = LoggerFactory.getLogger(JunitBaseTest.class);
    static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
    static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD = ThreadLocal.withInitial(TimeRecord::new);
    private static final ThreadLocal<Boolean> IS_CONFIGURED = ThreadLocal.withInitial(() -> false);
    private TestInfo testInfo;

    @BeforeAll
    public void beforeClassCore() {
        try {
            if (!IS_CONFIGURED.get()) {
                configure();
                IS_CONFIGURED.set(true);
            }
            var testClass = this.getClass();
            PluginExecutionEngine.preBeforeClass(testClass);
            beforeAll();
        } catch (Exception e) {
            log.error("An error occurred during the @BeforeAll execution phase.", e);
        }
    }

    @BeforeEach
    public void beforeMethodCore(TestInfo testInfo) {
        this.testInfo = testInfo;
        try {
            var testClass = this.getClass();
            var methodInfo = Arrays.stream(testClass.getMethods())
                    .filter(m -> m.getName().equals(testInfo.getTestMethod().get().getName()))
                    .findFirst()
                    .orElseThrow();
            PluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
            beforeEach();
        } catch (Exception e) {
            log.error("An error occurred during the @BeforeEach execution phase for test: {}", testInfo.getDisplayName(), e);
            PluginExecutionEngine.beforeTestFailed(e);
        }
    }

    @AfterEach
    public void afterMethodCore(TestInfo testInfo) {
        try {
            var testClass = this.getClass();
            var methodInfo = testClass.getMethod(testInfo.getTestMethod().get().getName(), testInfo.getTestMethod().get().getParameterTypes());

            Throwable testException = CURRENT_TEST_RESULT.get() == TestResult.FAILURE ? new RuntimeException("Test Failed") : null;
            PluginExecutionEngine.postAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo, testException);
            afterEach();
        } catch (Exception e) {
            log.error("An error occurred during the @AfterEach execution phase for test: {}", testInfo.getDisplayName(), e);
        }
    }

    @AfterAll
    public void afterClassCore() {
        try {
            var testClass = this.getClass();
            afterClass();
            PluginExecutionEngine.postAfterClass(testClass);
        } catch (Exception e) {
            log.error("An error occurred during the @AfterAll execution phase.", e);
        }
    }

    /**
     * Test classes override this method to register the plugins they need.
     */
    protected void configure() {
    }


    protected String getTestName() { return this.testInfo.getTestMethod().get().getName(); }
    protected void beforeAll() throws Exception {}
    protected void beforeEach() throws Exception {}
    protected void afterEach() {}
    protected void afterClass() {}
}