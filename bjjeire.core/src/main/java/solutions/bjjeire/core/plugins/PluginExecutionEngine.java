package solutions.bjjeire.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Executes lifecycle hooks on all registered {@link Plugin} beans.
 * This engine is managed by Spring and receives all plugin implementations
 * via constructor injection, decoupling the engine from plugin discovery.
 */
@Component
public final class PluginExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(PluginExecutionEngine.class);
    private final List<Plugin> plugins;

    @Autowired
    public PluginExecutionEngine(List<Plugin> plugins) {
        this.plugins = plugins;
        log.info("PluginExecutionEngine initialized with {} plugins.", plugins.size());
        plugins.forEach(p -> log.debug("Loaded Plugin: {}", p.getClass().getSimpleName()));
    }

    /**
     * Executes a given action on all registered plugins, with centralized error handling.
     *
     * @param action The operation to perform on each plugin.
     */
    private void executeOnPlugins(PluginConsumer action) {
        for (Plugin plugin : plugins) {
            try {
                action.accept(plugin);
            } catch (Exception e) {
                log.error("Error executing plugin {}", plugin.getClass().getSimpleName(), e);
            }
        }
    }

    public void preBeforeClass(Class<?> type) {
        log.trace("Executing preBeforeClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.preBeforeClass(type));
    }

    public void postBeforeClass(Class<?> type) {
        log.trace("Executing postBeforeClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.postBeforeClass(type));
    }

    public void beforeClassFailed(Exception e) {
        log.error("Executing beforeClassFailed due to exception.", e);
        executeOnPlugins(p -> p.beforeClassFailed(e));
    }

    public void preBeforeTest(TestResult result, Method memberInfo) {
        log.trace("Executing preBeforeTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.preBeforeTest(result, memberInfo));
    }

    public void postBeforeTest(TestResult result, Method memberInfo) {
        log.trace("Executing postBeforeTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.postBeforeTest(result, memberInfo));
    }

    public void beforeTestFailed(Exception e) {
        log.error("Executing beforeTestFailed due to exception.", e);
        executeOnPlugins(p -> p.beforeTestFailed(e));
    }

    public void preAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo) {
        log.trace("Executing preAfterTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.preAfterTest(result, timeRecord, memberInfo));
    }

    public void postAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        log.trace("Executing postAfterTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.postAfterTest(result, timeRecord, memberInfo, failedTestException));
    }

    public void afterTestFailed(Exception e) {
        log.error("Executing afterTestFailed due to exception.", e);
        executeOnPlugins(p -> p.afterTestFailed(e));
    }

    public void preAfterClass(Class<?> type) {
        log.trace("Executing preAfterClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.preAfterClass(type));
    }

    public void postAfterClass(Class<?> type) {
        log.trace("Executing postAfterClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.postAfterClass(type));
    }

    public void afterClassFailed(Exception e) {
        log.error("Executing afterClassFailed due to exception.", e);
        executeOnPlugins(p -> p.afterClassFailed(e));
    }

    @FunctionalInterface
    private interface PluginConsumer {
        void accept(Plugin plugin) throws Exception;
    }
}