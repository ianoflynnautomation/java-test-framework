package solutions.bjjeire.api.services;


import com.github.javafaker.Faker;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.TestClient;

public class App implements AutoCloseable {

    private final ApiClientService apiClientService;
    private final ApiSettings apiSettings;
    private final Faker faker;
    private boolean disposed = false;

    /**
     * Constructor for dependency injection.
     * @param apiClientService The shared, thread-safe API client service.
     * @param apiSettings The application configuration settings.
     * @param faker The shared instance for generating fake data.
     */
    public App(ApiClientService apiClientService, ApiSettings apiSettings, Faker faker) {
        this.apiClientService = apiClientService;
        this.apiSettings = apiSettings;
        this.faker = faker;
    }

    public Faker getFaker() {
        return faker;
    }

    public ApiClientService getApiClientService() {
        return apiClientService;
    }

    /**
     * Factory method to create a new, isolated TestClient for a single test.
     * @return A new TestClient instance.
     */
    public TestClient createTestClient() {
        if (disposed) {
            throw new IllegalStateException("The App container has been closed.");
        }
        // Each call creates a new client, ensuring test isolation.
        return new TestClient(apiClientService, apiSettings);
    }

    @Override
    public void close() {
        if (disposed) {
            return;
        }
        // The ApiClientService is closed here, which is managed by ApiTest's @AfterAll.
        if (apiClientService != null) {
            apiClientService.close();
        }
        disposed = true;
    }
}
