package solutions.bjjeire.core.plugins;

import java.lang.reflect.Method;

public interface Plugin {

    // == JUNIT-SPECIFIC HOOKS ==
    default void preBeforeClass(Class<?> type) throws Exception {}
    default void postAfterClass(Class<?> type) throws Exception {}
    default void preBeforeTest(TestResult testResult, Method memberInfo) throws Exception {}
    default void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) throws Exception {}

    // == CUCUMBER-SPECIFIC HOOKS (NEW) ==
    default void preBeforeScenario(ScenarioContext context) throws Exception {}
    default void postAfterScenario(ScenarioContext context) throws Exception {}

    // == GENERIC HOOKS ==
    default void beforeTestFailed(Exception e) throws Exception {}
    default void afterTestFailed(Exception e) throws Exception {}
    default void preAfterClass(Class<?> type) throws Exception {}
    default void afterClassFailed(Exception e) throws Exception {}
    default void postBeforeClass(Class<?> type) throws Exception {}
    default void postBeforeTest(TestResult testResult, Method memberInfo) throws Exception {}
    default void preAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo) throws Exception {}
}
