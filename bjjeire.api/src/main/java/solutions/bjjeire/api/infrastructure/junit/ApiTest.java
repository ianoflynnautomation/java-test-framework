package solutions.bjjeire.api.infrastructure.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.api.configuration.ApiTestConfiguration;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;

/**
 * A Spring-enabled base class for all non-Cucumber API test classes.
 * It automatically loads the Spring context and provides access to beans.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApiTestConfiguration.class)
public abstract class ApiTest extends JunitBaseTest {

    /**
     * The Spring Application Context, which acts as the dependency injection container.
     * It is autowired by the Spring TestContext Framework.
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The main entry point for creating a fluent API request for a test.
     * It retrieves a new prototype-scoped TestClient bean from the Spring context.
     *
     * @return A new, isolated TestClient instance for a single test.
     */
    protected TestClient when() {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext is not initialized. Ensure the test class is annotated correctly.");
        }
        // Retrieve a fresh, non-singleton instance of TestClient for each call.
        // This assumes TestClient is configured with a prototype or cucumber-glue scope.
        return applicationContext.getBean(TestClient.class);
    }
}
