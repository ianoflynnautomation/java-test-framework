package solutions.bjjeire.api.infrastructure.junit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.api.configuration.TestConfiguration;
import solutions.bjjeire.api.http.ApiClient;
import solutions.bjjeire.api.http.RequestSpecification;

import java.util.HashMap;

/**
 * A Spring-enabled base class for all API test classes.
 * It uses @SpringBootTest to load the full application context defined by
 * TestConfiguration.class, making all Spring beans available for injection.
 * It also provides a convenient factory method for creating isolated TestClient instances.
 */
@SpringBootTest(classes = TestConfiguration.class)
public abstract class ApiTestBase {

    @Autowired
    private ApiClient apiClient;

    protected RequestSpecification given() {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient is not initialized. Ensure the test class is Spring-enabled.");
        }
        
        return new RequestSpecification(apiClient, new HashMap<>(), new HashMap<>(), null, null);
    }
}