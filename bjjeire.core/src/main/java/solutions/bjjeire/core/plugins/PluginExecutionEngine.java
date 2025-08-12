package solutions.bjjeire.core.plugins;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;

@Slf4j
public final class PluginExecutionEngine {

    @FunctionalInterface
    private interface PluginAction {
        void accept(Plugin plugin) throws Exception;
    }

    private PluginExecutionEngine() {
    }

    private static void execute(PluginAction action) {
        for (Plugin plugin : UsesPlugins.getPlugins()) {
            try {
                action.accept(plugin);
            } catch (Exception e) {
                log.error("Plugin execution failed",
                        StructuredArguments.keyValue("eventName", "pluginExecutionFailed"),
                        StructuredArguments.keyValue("pluginClass", plugin.getClass().getSimpleName()),
                        e);
            }
        }
    }

    public static void preBeforeClass(Class<?> type) {
        log.info("Executing preBeforeClass hook",
                StructuredArguments.keyValue("eventName", "preBeforeClass"),
                StructuredArguments.keyValue("testClass", type.getSimpleName()));
        execute(p -> p.preBeforeClass(type));
    }

    public static void postAfterClass(Class<?> type) {
        log.info("Executing postAfterClass hook",
                StructuredArguments.keyValue("eventName", "postAfterClass"),
                StructuredArguments.keyValue("testClass", type.getSimpleName()));
        execute(p -> p.postAfterClass(type));
        UsesPlugins.VETO_PLUGINS();
    }

    public static void preBeforeTest(TestResult result, Method memberInfo) {
        log.info("Executing preBeforeTest hook",
                StructuredArguments.keyValue("eventName", "preBeforeTest"),
                StructuredArguments.keyValue("testMethod", memberInfo.getName()));
        execute(p -> p.preBeforeTest(result, memberInfo));
    }

    public static void postAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo,
            Throwable failedTestException) {
        log.info("Executing postAfterTest hook",
                StructuredArguments.keyValue("eventName", "postAfterTest"),
                StructuredArguments.keyValue("testMethod", memberInfo.getName()),
                StructuredArguments.keyValue("testStatus", result != null ? result.name() : "UNKNOWN"));
        execute(p -> p.postAfterTest(result, timeRecord, memberInfo, failedTestException));
    }

    public static void preBeforeScenario(ScenarioContext context) {
        log.info("Executing preBeforeScenario hook",
                StructuredArguments.keyValue("eventName", "preBeforeScenario"),
                StructuredArguments.keyValue("scenarioName", context.getScenarioName()));
        execute(p -> p.preBeforeScenario(context));
    }

    public static void postAfterScenario(ScenarioContext context) {
        log.info("Executing postAfterScenario hook",
                StructuredArguments.keyValue("eventName", "postAfterScenario"),
                StructuredArguments.keyValue("scenarioName", context.getScenarioName()));
        execute(p -> p.postAfterScenario(context));
    }

    public static void beforeTestFailed(Exception e) {
        log.error("Executing test failure hook",
                StructuredArguments.keyValue("eventName", "beforeTestFailed"),
                e);
        execute(p -> p.beforeTestFailed(e));
    }
}