package solutions.bjjeire.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;

public final class PluginExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(PluginExecutionEngine.class);

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
                log.error("Error executing plugin {}", plugin.getClass().getSimpleName(), e);
            }
        }
    }

    // --- GENERIC LIFECYCLE HOOKS ---
    public static void preBeforeClass(Class<?> type) {
        log.trace("Executing preBeforeClass for type: {}", type.getSimpleName());
        execute(p -> p.preBeforeClass(type));
    }

    public static void postAfterClass(Class<?> type) {
        log.trace("Executing postAfterClass for type: {}", type.getSimpleName());
        execute(p -> p.postAfterClass(type));
        UsesPlugins.VETO_PLUGINS();
    }

    // --- JUNIT-SPECIFIC HOOKS ---
    public static void preBeforeTest(TestResult result, Method memberInfo) {
        log.trace("Executing preBeforeTest for method: {}", memberInfo.getName());
        execute(p -> p.preBeforeTest(result, memberInfo));
    }

    public static void postAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        log.trace("Executing postAfterTest for method: {}", memberInfo.getName());
        execute(p -> p.postAfterTest(result, timeRecord, memberInfo, failedTestException));
    }

    // --- CUCUMBER-SPECIFIC HOOKS ---
    public static void preBeforeScenario(ScenarioContext context) {
        log.trace("Executing preBeforeScenario for: {}", context.getScenarioName());
        execute(p -> p.preBeforeScenario(context));
    }

    public static void postAfterScenario(ScenarioContext context) {
        log.trace("Executing postAfterScenario for: {}", context.getScenarioName());
        execute(p -> p.postAfterScenario(context));
    }

    // --- FAILURE HOOKS ---
    public static void beforeTestFailed(Exception e) {
        log.error("Executing beforeTestFailed due to exception.", e);
        execute(p -> p.beforeTestFailed(e));
    }
}