package solutions.bjjeire.cucumber.context;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("cucumber-glue")
public class GymContext extends BaseContext {
    private final List<String> createdGymIds = new ArrayList<>();

    public List<String> getCreatedGymIds() { return createdGymIds; }
    public void addAllCreatedGymIds(List<String> ids) { this.createdGymIds.addAll(ids); }
}