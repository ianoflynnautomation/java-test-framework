package solutions.bjjeire.core.plugins;

import java.lang.reflect.Method;

/**
 * Defines the contract for a test lifecycle plugin.
 * Concrete implementations of this interface should be annotated with @Component
 * to be discovered and managed by Spring.
 */
public interface Plugin {

    default void preBeforeClass(Class<?> type) throws Exception {}
    default void postBeforeClass(Class<?> type) throws Exception {}
    default void beforeClassFailed(Exception e) throws Exception {}
    default void preBeforeTest(TestResult testResult, Method memberInfo) throws Exception {}
    default void postBeforeTest(TestResult testResult, Method memberInfo) throws Exception {}
    default void beforeTestFailed(Exception e) throws Exception {}
    default void preAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo) throws Exception {}
    default void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) throws Exception {}
    default void afterTestFailed(Exception e) throws Exception {}
    default void preAfterClass(Class<?> type) throws Exception {}
    default void postAfterClass(Class<?> type) throws Exception {}
    default void afterClassFailed(Exception e) throws Exception {}
}
