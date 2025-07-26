package solutions.bjjeire.cucumber.context;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("cucumber-glue")
public class EventContext extends BaseContext {
    private final List<String> createdEventIds = new ArrayList<>();

    public List<String> getCreatedEventIds() { return createdEventIds; }
    public void addAllCreatedEventIds(List<String> ids) { this.createdEventIds.addAll(ids); }
}
