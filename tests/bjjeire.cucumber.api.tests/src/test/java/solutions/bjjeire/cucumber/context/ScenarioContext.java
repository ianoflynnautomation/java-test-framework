package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ValidatableResponse;

import java.util.ArrayList;
import java.util.List;

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
