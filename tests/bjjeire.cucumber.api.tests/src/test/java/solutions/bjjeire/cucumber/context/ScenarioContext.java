package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.GetBjjEventPaginatedResponse;
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
    private ValidatableResponse lastResponse;
    private final List<Object> createdEntities = new ArrayList<>();
}
