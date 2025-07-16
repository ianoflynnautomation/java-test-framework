package solutions.bjjeire.api.services;

import com.github.javafaker.Faker;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.http.TestClient;

/**
 * A Spring-managed component that provides access to shared services and factory methods.
 * This class is a singleton managed by the Spring container.
 * Note: In a pure Spring architecture, this class can often be eliminated,
 * with components like ApiTest directly injecting the beans they need.
 */
@Component
public class App implements AutoCloseable {

    private final IApiClientService apiClientService;
    private final ObjectProvider<TestClient> testClientProvider;
    private final Faker faker;
    private boolean disposed = false;

    /**
     * Constructor for Spring dependency injection.
     * @param apiClientService The shared, thread-safe API client service.
     * @param testClientProvider A provider for creating new, isolated instances of TestClient.
     * @param faker The shared instance for generating fake data.
     */
    @Autowired
    public App(IApiClientService apiClientService, ObjectProvider<TestClient> testClientProvider, Faker faker) {
        this.apiClientService = apiClientService;
        this.testClientProvider = testClientProvider;
        this.faker = faker;
    }

    public Faker getFaker() {
        return faker;
    }

    public IApiClientService getApiClientService() {
        return apiClientService;
    }

    /**
     * Factory method to create a new, isolated TestClient for a single test.
     * @return A new TestClient instance with the appropriate scope (e.g., prototype).
     */
    public TestClient createTestClient() {
        if (disposed) {
            throw new IllegalStateException("The App container has been closed.");
        }
        // Use the ObjectProvider to get a new instance of TestClient.
        // This is the Spring-native way to handle prototype-scoped beans inside a singleton.
        return testClientProvider.getObject();
    }

    /**
     * This method is automatically called by Spring when the application context is closed.
     * It ensures that underlying resources like the HTTP client's connection pool are properly shut down.
     * The @PreDestroy annotation requires the jakarta.annotation-api dependency.
     */
    @Override
    @PreDestroy
    public void close() {
        if (disposed) {
            return;
        }
        if (apiClientService != null) {
            apiClientService.close();
        }
        disposed = true;
    }
}
