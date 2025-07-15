package solutions.bjjeire.cucumber.context;


import lombok.Getter;
import lombok.Setter;
import solutions.bjjeire.api.validation.ResponseAsserter;

@Getter
@Setter
public class ScenarioContext {

    private Object requestPayload;
    private ResponseAsserter<?> responseAsserter;
    private String eventName;
    private String authToken;
}
