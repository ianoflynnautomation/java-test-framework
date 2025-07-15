package solutions.bjjeire.core.plugins;

import java.lang.reflect.Method;
import java.util.*;

public final class PluginExecutionEngine {
    private final static LinkedHashSet<Plugin> PLUGINS;

    static {
        PLUGINS = new LinkedHashSet<>();
    }

    public static void addPlugin(Plugin plugin) {
        PLUGINS.add(plugin);
    }

    public static void removePlugin(Plugin plugin) {
        PLUGINS.remove(plugin);
    }

    public static void preBeforeClass(Class type) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.preBeforeClass(type);
        }
    }

    public static void postBeforeClass(Class type) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.postBeforeClass(type);
        }
    }

    public static void beforeClassFailed(Exception e) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.beforeClassFailed(e);
        }
    }

    public static void preBeforeTest(TestResult result, Method memberInfo) throws Exception {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.preBeforeTest(result, memberInfo);
        }
    }

    public static void postBeforeTest(TestResult result, Method memberInfo) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.postBeforeTest(result, memberInfo);
        }
    }

    public static void beforeTestFailed(Exception e) throws Exception {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.beforeTestFailed(e);
        }
    }

    public static void preAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo) throws Exception {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.preAfterTest(result, timeRecord, memberInfo);
        }
    }

    public static void postAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.postAfterTest(result, timeRecord, memberInfo, failedTestException);
        }
    }

    public static void afterTestFailed(Exception e) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.afterTestFailed(e);
        }
    }

    public static void preAfterClass(Class type) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.preAfterClass(type);
        }
    }

    public static void postAfterClass(Class type) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.postAfterClass(type);
        }
    }

    public static void afterClassFailed(Exception e) {
        for (var currentObserver : PLUGINS) {
            if (currentObserver != null)
                currentObserver.afterClassFailed(e);
        }
    }
}
