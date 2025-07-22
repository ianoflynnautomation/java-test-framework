package solutions.bjjeire.core.plugins.testng;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.ITestResult;
import org.testng.annotations.*;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;

/**
 * A generic, Spring-enabled base class for TestNG tests.
 * 1. Extends AbstractTestNGSpringContextTests for core Spring-TestNG integration.
 * 2. Injects the PluginExecutionEngine.
 * 3. Does NOT specify a @ContextConfiguration, making it reusable for different test types.
 */
@Listeners(TestResultListener.class)
public abstract class BaseTest {

    @Autowired
    protected PluginExecutionEngine pluginExecutionEngine;

    static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
    static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD = ThreadLocal.withInitial(TimeRecord::new);
    private static final ThreadLocal<Boolean> CONFIGURATION_EXECUTED = ThreadLocal.withInitial(() -> false);

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        try {
            if (!CONFIGURATION_EXECUTED.get()) {
                configure();
                CONFIGURATION_EXECUTED.set(true);
            }
            var testClass = this.getClass();
            pluginExecutionEngine.preBeforeClass(testClass);
            beforeAll();
            pluginExecutionEngine.postBeforeClass(testClass);
        } catch (Exception e) {
            pluginExecutionEngine.beforeClassFailed(e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethodCore(ITestResult result) throws Exception {
        try {
            var methodInfo = this.getClass().getMethod(result.getMethod().getMethodName());
            pluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
            beforeEach();
            pluginExecutionEngine.postBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
        } catch (Exception e) {
            e.printStackTrace();
            pluginExecutionEngine.beforeTestFailed(e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethodCore(ITestResult result) {
        try {
            var testClass = this.getClass();
            var methodInfo = testClass.getMethod(result.getMethod().getMethodName());
            pluginExecutionEngine.preAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo);
            afterEach();
            pluginExecutionEngine.postAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo, result.getThrowable());
        } catch (Exception e) {
            pluginExecutionEngine.afterTestFailed(e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClassCore() {
        try {
            var testClass = this.getClass();
            pluginExecutionEngine.preAfterClass(testClass);
            afterAll();
            pluginExecutionEngine.postAfterClass(testClass);
        } catch (Exception e) {
            pluginExecutionEngine.afterClassFailed(e);
        }
    }

    // Hook methods for subclasses to override
    protected void configure() {}
    protected void beforeAll() throws Exception {}
    protected void afterAll() throws Exception {}
    protected void beforeEach() throws Exception {}
    protected void afterEach() {}
}
