package solutions.bjjeire.core.plugins;

import java.io.IOException;
import java.lang.reflect.Method;

public abstract class Plugin {

    public Plugin() {
        PluginExecutionEngine.addPlugin(this);
    }

    public void preBeforeClass(Class type) {
    }

    public void postBeforeClass(Class type) {
    }

    public void beforeClassFailed(Exception e) {
    }

    public void preBeforeTest(TestResult testResult, Method memberInfo) throws Exception {
    }

    public void postBeforeTest(TestResult testResult, Method memberInfo) {
    }

    public void beforeTestFailed(Exception e) throws Exception {
    }
    public void preAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo) throws IOException {
    }
    public void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
    }

    public void afterTestFailed(Exception e) {
    }

    public void preAfterClass(Class type) {
    }

    public void postAfterClass(Class type) {
    }

    public void afterClassFailed(Exception e) {
    }

}
