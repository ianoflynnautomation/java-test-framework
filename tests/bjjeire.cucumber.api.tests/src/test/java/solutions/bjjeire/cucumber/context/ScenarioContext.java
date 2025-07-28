package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A scenario-scoped bean to hold state within a single Cucumber scenario.
 * This ensures that data from one test does not leak into another.
 */
@Component
@ScenarioScope
@Getter
@Setter
public class ScenarioContext {

    private String authToken;
    private Object requestPayload;
    private String eventName;
    private String gymName;
    private ResponseAsserter responseAsserter;
    private BjjEvent createdEvent;
    private Gym createdGym;

    private final List<Consumer<TestClient>> cleanupActions = new ArrayList<>();

    public void addCleanupAction(Consumer<TestClient> action) {
        cleanupActions.add(action);
    }
}
