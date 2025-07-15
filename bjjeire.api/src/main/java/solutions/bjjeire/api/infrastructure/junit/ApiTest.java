package solutions.bjjeire.api.infrastructure.junit;


import com.github.javafaker.Faker;
import org.testng.annotations.TestInstance;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.services.ApiClientService;
import solutions.bjjeire.api.services.App;
import solutions.bjjeire.core.configuration.EnvironmentConfigurationProvider;
import solutions.bjjeire.core.configuration.IConfigurationProvider;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;

/**
 * A base class for all non-Cucumber API test classes, integrating DI setup
 * with the custom plugin execution lifecycle from JunitBaseTest.
 *
 * It initializes an 'App' container once per test class by hooking into
 * the beforeAll() method from its parent.
 */
public abstract class ApiTest extends JunitBaseTest {

    private App app;

    /**
     * Overrides the lifecycle hook from JunitBaseTest to set up the
     * DI container before any tests in the class run.
     */
    @Override
    protected void beforeAll() throws Exception {
        super.beforeAll(); // Ensures parent class logic runs first
        // Manually perform dependency injection for the JUnit test context.
        IConfigurationProvider configProvider = new EnvironmentConfigurationProvider();
        ApiSettings apiSettings = configProvider.getSettings(ApiSettings.class);
        ApiClientService apiClientService = new ApiClientService(apiSettings);
        Faker faker = new Faker();
        this.app = new App(apiClientService, apiSettings, faker);
    }

    /**
     * Overrides the lifecycle hook from JunitBaseTest to tear down the
     * DI container after all tests in the class have completed.
     */
    @Override
    protected void afterClass() {
        if (app != null) {
            app.close();
        }
        super.afterClass(); // Ensures parent class logic runs last
    }

    /**
     * Provides access to the App container, which holds shared services.
     * @return The App instance for the current test class.
     */
    protected App app() {
        if (app == null) {
            throw new IllegalStateException("The App container is not initialized. Ensure beforeAll() has run successfully.");
        }
        return app;
    }

    /**
     * The main entry point for creating a fluent API request for a test.
     * This delegates to the App container to create a new, isolated client.
     * @return A new TestClient instance.
     */
    protected TestClient when() {
        return app().createTestClient();
    }
}