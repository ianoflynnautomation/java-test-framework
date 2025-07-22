package solutions.bjjeire.core.plugins.junit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({TestResultWatcher.class, TestDurationWatcher.class})
public abstract class JunitBaseTest {

    private static final Logger log = LoggerFactory.getLogger(JunitBaseTest.class);

    @Autowired
    protected PluginExecutionEngine pluginExecutionEngine;

    static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
    static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD = ThreadLocal.withInitial(TimeRecord::new);

    private TestInfo testInfo;

    @BeforeAll
    public void beforeClassCore() {
        try {
            configure();
            var testClass = this.getClass();
            pluginExecutionEngine.preBeforeClass(testClass);
            beforeAll();
            pluginExecutionEngine.postBeforeClass(testClass);
        } catch (Exception e) {
            log.error("An error occurred during the @BeforeAll execution phase.", e);
            pluginExecutionEngine.beforeClassFailed(e);
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
            pluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
            beforeEach();
            pluginExecutionEngine.postBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
        } catch (Exception e) {
            log.error("An error occurred during the @BeforeEach execution phase for test: {}", testInfo.getDisplayName(), e);
            onBeforeEachFailure();
            pluginExecutionEngine.beforeTestFailed(e);
        }
    }

    @AfterEach
    public void afterMethodCore(TestInfo testInfo) {
        try {
            var testClass = this.getClass();
            var methodInfo = testClass.getMethod(testInfo.getTestMethod().get().getName(), testInfo.getTestMethod().get().getParameterTypes());
            pluginExecutionEngine.preAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo);
            afterEach();
        } catch (Exception e) {
            log.error("An error occurred during the @AfterEach execution phase for test: {}", testInfo.getDisplayName(), e);
            pluginExecutionEngine.afterTestFailed(e);
        }
    }

    @AfterAll
    public void afterClassCore() {
        try {
            var testClass = this.getClass();
            pluginExecutionEngine.preAfterClass(testClass);
            afterClass();
            pluginExecutionEngine.postAfterClass(testClass);
        } catch (Exception e) {
            log.error("An error occurred during the @AfterAll execution phase.", e);
            pluginExecutionEngine.afterClassFailed(e);
        }
    }

    protected String getTestName() {
        return this.testInfo.getTestMethod().get().getName();
    }

    // Hook methods for subclasses
    protected void configure() {}
    protected void beforeAll() throws Exception {}
    protected void beforeEach() throws Exception {}
    protected void onBeforeEachFailure() {}
    protected void afterEach() {}
    protected void afterClass() {}
}