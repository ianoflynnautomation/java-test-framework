package solutions.bjjeire.cucumber.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import solutions.bjjeire.api.validation.ApiResponse;

@Component
@ScenarioScope
@Getter
public class TestContext {
    @Setter
    private String authToken;
    @Setter
    private Object requestPayload;
    @Setter
    private ApiResponse lastResponse;
    
    private final List<Object> createdEntities = new ArrayList<>();

    public void addEntityForCleanup(Object entity) {
        if (entity != null) {
            this.createdEntities.add(entity);
        }
    }

}
