package solutions.bjjeire.core.plugins.junit;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;
import solutions.bjjeire.core.plugins.UsesPlugins;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({ TestResultWatcher.class, TestDurationWatcher.class })
public abstract class JunitBaseTest extends UsesPlugins {

    static final ThreadLocal<TestResult> CURRENT_TEST_RESULT = new ThreadLocal<>();
    static final ThreadLocal<TimeRecord> CURRENT_TEST_TIME_RECORD = ThreadLocal.withInitial(TimeRecord::new);
    private static final ThreadLocal<Boolean> IS_CONFIGURED = ThreadLocal.withInitial(() -> false);
    private TestInfo testInfo;

    @BeforeAll
    public void beforeClassCore() {
        var testClassName = this.getClass().getSimpleName();
        log.info("Executing @BeforeAll phase", StructuredArguments.keyValue("testClass", testClassName));
        try {
            if (!IS_CONFIGURED.get()) {
                configure();
                IS_CONFIGURED.set(true);
            }
            PluginExecutionEngine.preBeforeClass(this.getClass());
            beforeAll();
        } catch (Exception e) {
            log.error("Error during @BeforeAll phase",
                    StructuredArguments.keyValue("testClass", testClassName),
                    e);
        }
    }

    @BeforeEach
    public void beforeMethodCore(TestInfo testInfo) {
        this.testInfo = testInfo;
        log.info("Executing @BeforeEach phase", StructuredArguments.keyValue("testName", testInfo.getDisplayName()));
        try {
            var testClass = this.getClass();
            var methodInfo = Arrays.stream(testClass.getMethods())
                    .filter(m -> m.getName().equals(testInfo.getTestMethod().get().getName()))
                    .findFirst()
                    .orElseThrow();
            PluginExecutionEngine.preBeforeTest(CURRENT_TEST_RESULT.get(), methodInfo);
            beforeEach();
        } catch (Exception e) {
            log.error("Error during @BeforeEach phase",
                    StructuredArguments.keyValue("testName", testInfo.getDisplayName()),
                    e);
            PluginExecutionEngine.beforeTestFailed(e);
        }
    }

    @AfterEach
    public void afterMethodCore(TestInfo testInfo) {
        log.info("Executing @AfterEach phase", StructuredArguments.keyValue("testName", testInfo.getDisplayName()));
        try {
            var testClass = this.getClass();
            var methodInfo = testClass.getMethod(testInfo.getTestMethod().get().getName(),
                    testInfo.getTestMethod().get().getParameterTypes());

            Throwable testException = CURRENT_TEST_RESULT.get() == TestResult.FAILURE
                    ? new RuntimeException("Test Failed")
                    : null;
            PluginExecutionEngine.postAfterTest(CURRENT_TEST_RESULT.get(), CURRENT_TEST_TIME_RECORD.get(), methodInfo,
                    testException);
            afterEach();
        } catch (Exception e) {
            log.error("Error during @AfterEach phase",
                    StructuredArguments.keyValue("testName", testInfo.getDisplayName()),
                    e);
        }
    }

    @AfterAll
    public void afterClassCore() {
        var testClassName = this.getClass().getSimpleName();
        log.info("Executing @AfterAll phase", StructuredArguments.keyValue("testClass", testClassName));
        try {
            afterClass();
            PluginExecutionEngine.postAfterClass(this.getClass());
        } catch (Exception e) {
            log.error("Error during @AfterAll phase",
                    StructuredArguments.keyValue("testClass", testClassName),
                    e);
        }
    }

    protected void configure() {
    }

    protected String getTestName() {
        return this.testInfo.getTestMethod().get().getName();
    }

    protected void beforeAll() throws Exception {
    }

    protected void beforeEach() throws Exception {
    }

    protected void afterEach() {
    }

    protected void afterClass() {
    }
}