package solutions.bjjeire.core.plugins.testng;

import org.testng.ITestResult;
import org.testng.annotations.*;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;
import solutions.bjjeire.core.plugins.UsesPlugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Listeners(TestResultListener.class)
public class BaseTest extends UsesPlugins {

    static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
    static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD = ThreadLocal.withInitial(TimeRecord::new);
    private static final ThreadLocal<Boolean> CONFIGURATION_EXECUTED = new ThreadLocal<>();
    private static final List<String> ALREADY_EXECUTED_BEFORE_CLASSES = Collections.synchronizedList(new ArrayList<>());

    public BaseTest() {
        CONFIGURATION_EXECUTED.set(false);
    }

    @BeforeClass
    public void beforeClass() {
        try {
            if (!CONFIGURATION_EXECUTED.get()) {
                configure();
                CONFIGURATION_EXECUTED.set(true);
            }
            var testClass = this.getClass();
            PluginExecutionEngine.preBeforeClass(testClass);
            beforeAll();
            PluginExecutionEngine.postBeforeClass(testClass);
        } catch (Exception e) {
            PluginExecutionEngine.beforeClassFailed(e);
        }
    }

    @BeforeMethod
    public void beforeMethodCore(ITestResult result) throws Exception {
        try {
            var methodInfo = this.getClass().getMethod(result.getMethod().getMethodName());
            PluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
            beforeEach();
            PluginExecutionEngine.postBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
        } catch (Exception e) {
            e.printStackTrace();
            PluginExecutionEngine.beforeTestFailed(e);
        }
    }

    @AfterMethod
    public void afterMethodCore(ITestResult result) {
        try {
            var testClass = this.getClass();
            var methodInfo = testClass.getMethod(result.getMethod().getMethodName());
            PluginExecutionEngine.preAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo);
            afterEach();
            PluginExecutionEngine.postAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo, result.getThrowable());

        } catch (Exception e) {
            PluginExecutionEngine.afterTestFailed(e);
        }
    }

    @AfterClass
    public void afterClassCore() {
        try {
            var testClass = this.getClass();
            PluginExecutionEngine.preAfterClass(testClass);
            afterAll();
            PluginExecutionEngine.postAfterClass(testClass);
        } catch (Exception e) {
            PluginExecutionEngine.afterClassFailed(e);
        }
    }
    protected void configure() {
    }

    protected void beforeAll() throws Exception {
    }

    protected void afterAll() throws Exception {
    }

    protected void beforeEach() throws Exception {
    }

    protected void afterEach() {
    }
}
