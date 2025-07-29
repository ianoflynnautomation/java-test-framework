package solutions.bjjeire.cucumber.steps;

import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.http.ApiClient;
import solutions.bjjeire.api.http.RequestSpecification;
import solutions.bjjeire.cucumber.configuration.CucumberSpringConfiguration;

import java.util.HashMap;

public abstract class CucumberTestBase extends CucumberSpringConfiguration {
    
    @Autowired
    private ApiClient apiClient;

    protected RequestSpecification given() {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient is not initialized. Ensure the step definition class is Spring-enabled.");
        }
        return new RequestSpecification(apiClient, new HashMap<>(), new HashMap<>(), null, null);
    }
}
