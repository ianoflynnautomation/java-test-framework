package solutions.bjjeire.api.infrastructure.junit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.configuration.TestConfiguration;
import solutions.bjjeire.api.http.TestClient;

/**
 * A Spring-enabled base class for all API test classes.
 * It uses @SpringBootTest to load the full application context defined by
 * TestConfiguration.class, making all Spring beans available for injection.
 * It also provides a convenient factory method for creating isolated TestClient instances.
 */
@SpringBootTest(classes = TestConfiguration.class)
public abstract class ApiTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The main entry point for creating a fluent API request for a test.
     * It retrieves a new prototype-scoped TestClient bean from the Spring context.
     * This ensures each test gets a clean, isolated instance, which is crucial
     * for parallel execution and preventing state leakage.
     *
     * @return A new, isolated TestClient instance for building and executing a request.
     */
    protected TestClient testClient() {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext is not initialized. Ensure the test class is annotated correctly.");
        }
        // Retrieve a fresh, prototype-scoped instance of TestClient for each call.
        return applicationContext.getBean(TestClient.class);
    }
}