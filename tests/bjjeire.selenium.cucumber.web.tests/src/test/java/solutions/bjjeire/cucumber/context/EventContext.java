package solutions.bjjeire.cucumber.context;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@Scope("cucumber-glue")
public class EventContext {
    private final List<String> createdEventIds = new ArrayList<>();

    public void addAllCreatedEventIds(List<String> ids) {
        this.createdEventIds.addAll(ids);
    }
}
