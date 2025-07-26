package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.api.validation.ResponseAsserter;

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
    private ResponseAsserter responseAsserter;
    private BjjEvent createdEvent;

}
