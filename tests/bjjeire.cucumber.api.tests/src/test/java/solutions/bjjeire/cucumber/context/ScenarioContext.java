package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
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
    private BjjEvent createdEvent;
    private Gym createdGym;
    private ValidatableResponse validatableResponse;
    private final List<Runnable> cleanupActions = new ArrayList<>();

    public void addCleanupAction(Runnable action) {
        cleanupActions.add(action);
    }

    public void runCleanupActions() {
        cleanupActions.forEach(Runnable::run);
        cleanupActions.clear();
    }
}
