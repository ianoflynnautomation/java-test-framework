package solutions.bjjeire.cucumber.context;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@Scope("cucumber-glue")
public class GymContext  {
    private final List<String> createdGymIds = new ArrayList<>();

    public void addAllCreatedGymIds(List<String> ids) {
        this.createdGymIds.addAll(ids);
    }
}