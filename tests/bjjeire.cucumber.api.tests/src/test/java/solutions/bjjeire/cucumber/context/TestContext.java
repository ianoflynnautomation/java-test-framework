package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ApiResponse;

import java.util.ArrayList;
import java.util.List;

@Component
@ScenarioScope
@Getter
public class TestContext {
    @Setter private String authToken;
    @Setter private Object requestPayload;
    @Setter private ApiResponse lastResponse;
    private final List<Object> createdEntities = new ArrayList<>();

    public void addEntityForCleanup(Object entity) {
        if (entity != null) {
            this.createdEntities.add(entity);
        }
    }


}
