package solutions.bjjeire.cucumber.context;

import com.github.javafaker.Faker;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.PicoFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.services.ApiClientService;
import solutions.bjjeire.core.configuration.EnvironmentConfigurationProvider;
import solutions.bjjeire.core.configuration.IConfigurationProvider;
import org.picocontainer.behaviors.Caching;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom ObjectFactory to configure PicoContainer for dependency injection.
 * This class is the central point for defining object lifecycles (scopes).
 */
public class PicoDependencyInjector implements ObjectFactory {

    // The parent container holds components that live for the entire test run.
    private static final MutablePicoContainer PARENT_CONTAINER = createParentContainer();
    private static volatile boolean isShutdownHookRegistered = false;

    private final List<Class<?>> glueClasses = new ArrayList<>();
    private MutablePicoContainer scenarioContainer;

    private static MutablePicoContainer createParentContainer() {
        MutablePicoContainer container = new DefaultPicoContainer(new Caching());
        // SINGLETONS: One instance shared across all scenarios.
        container.addComponent(IConfigurationProvider.class, EnvironmentConfigurationProvider.class);
        container.addComponent(ApiClientService.class);
        container.addComponent(Faker.class);
        // Resolve ApiSettings from the provider and register the instance as a component.
        ApiSettings settings = container.getComponent(IConfigurationProvider.class).getSettings(ApiSettings.class);
        container.addComponent(settings);
        container.start();
        return container;
    }

    public PicoDependencyInjector() {
        // A JVM shutdown hook is the most reliable way to ensure the parent container's
        // resources (like the HTTP connection pool) are cleaned up at the very end.
        if (!isShutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (PARENT_CONTAINER != null) {
                    PARENT_CONTAINER.stop();
                    PARENT_CONTAINER.dispose();
                }
            }));
            isShutdownHookRegistered = true;
        }
    }

    @Override
    public void start() {
        // This is called before each scenario.
        // We create a new child container that inherits singletons from the parent.
        this.scenarioContainer = PARENT_CONTAINER.makeChildContainer();

        // SCENARIO SCOPE: Register classes that need a fresh instance for each scenario.
        scenarioContainer.addComponent(ScenarioContext.class);
        scenarioContainer.addComponent(TestClient.class);

        // Register all glue (step/hook) classes found by Cucumber into the scenario scope.
        for (Class<?> glueClass : glueClasses) {
            scenarioContainer.addComponent(glueClass);
        }
        scenarioContainer.start();
    }

    @Override
    public void stop() {
        // This is called after each scenario to dispose of the scenario-scoped container.
        if (this.scenarioContainer != null) {
            this.scenarioContainer.stop();
            this.scenarioContainer.dispose();
        }
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        // Cucumber calls this to inform us of the step and hook classes it has found.
        glueClasses.add(glueClass);
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        // Cucumber calls this to get instances of the glue classes for a scenario.
        return this.scenarioContainer.getComponent(type);
    }
}