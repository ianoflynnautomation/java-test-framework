package solutions.bjjeire.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Refactored as a Spring-managed bean. It is no longer a static class.
 * Spring automatically discovers and injects all beans of type 'Plugin' into the constructor.
 * This decouples the engine from plugin creation and management.
 */
@Component
public final class PluginExecutionEngine {

    private static final Logger logger = LoggerFactory.getLogger(PluginExecutionEngine.class);
    private final List<Plugin> plugins;

    /**
     * The list of all available plugins is injected by the Spring container.
     * Any class that implements the Plugin interface and is annotated with @Component
     * will be automatically added to this list.
     *
     * @param plugins A list of all Plugin beans found in the Spring context.
     */
    @Autowired
    public PluginExecutionEngine(List<Plugin> plugins) {
        this.plugins = plugins;
        logger.info("PluginExecutionEngine initialized with {} plugins.", plugins.size());
        plugins.forEach(p -> logger.debug("Loaded Plugin: {}", p.getClass().getSimpleName()));
    }

    /**
     * A robust, reusable helper method to execute an action on all registered plugins.
     * It includes error handling to ensure that one faulty plugin does not prevent
     * others from executing.
     *
     * @param action The operation to perform on each plugin.
     */
    private void executeOnPlugins(PluginConsumer action) {
        for (Plugin plugin : plugins) {
            try {
                action.accept(plugin);
            } catch (Exception e) {
                logger.error("Error executing plugin {}", plugin.getClass().getSimpleName(), e);
                // Depending on requirements, you might want to rethrow or handle this differently.
                // For now, we log the error and continue.
            }
        }
    }

    // All public methods are now instance methods that use the injected list of plugins.

    public void preBeforeClass(Class<?> type) {
        logger.trace("Executing preBeforeClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.preBeforeClass(type));
    }

    public void postBeforeClass(Class<?> type) {
        logger.trace("Executing postBeforeClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.postBeforeClass(type));
    }

    public void beforeClassFailed(Exception e) {
        logger.error("Executing beforeClassFailed due to exception.", e);
        executeOnPlugins(p -> p.beforeClassFailed(e));
    }

    public void preBeforeTest(TestResult result, Method memberInfo) {
        logger.trace("Executing preBeforeTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.preBeforeTest(result, memberInfo));
    }

    public void postBeforeTest(TestResult result, Method memberInfo) {
        logger.trace("Executing postBeforeTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.postBeforeTest(result, memberInfo));
    }

    public void beforeTestFailed(Exception e) {
        logger.error("Executing beforeTestFailed due to exception.", e);
        executeOnPlugins(p -> p.beforeTestFailed(e));
    }

    public void preAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo) {
        logger.trace("Executing preAfterTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.preAfterTest(result, timeRecord, memberInfo));
    }

    public void postAfterTest(TestResult result, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        logger.trace("Executing postAfterTest for method: {}", memberInfo.getName());
        executeOnPlugins(p -> p.postAfterTest(result, timeRecord, memberInfo, failedTestException));
    }

    public void afterTestFailed(Exception e) {
        logger.error("Executing afterTestFailed due to exception.", e);
        executeOnPlugins(p -> p.afterTestFailed(e));
    }

    public void preAfterClass(Class<?> type) {
        logger.trace("Executing preAfterClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.preAfterClass(type));
    }

    public void postAfterClass(Class<?> type) {
        logger.trace("Executing postAfterClass for type: {}", type.getSimpleName());
        executeOnPlugins(p -> p.postAfterClass(type));
    }

    public void afterClassFailed(Exception e) {
        logger.error("Executing afterClassFailed due to exception.", e);
        executeOnPlugins(p -> p.afterClassFailed(e));
    }


    @FunctionalInterface
    private interface PluginConsumer {
        void accept(Plugin plugin) throws Exception;
    }
}