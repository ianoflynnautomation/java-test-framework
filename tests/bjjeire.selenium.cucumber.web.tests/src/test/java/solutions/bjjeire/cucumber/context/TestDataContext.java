package solutions.bjjeire.cucumber.context;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TestDataContext {

    private final Map<Class<?>, List<String>> createdEntityIds = new ConcurrentHashMap<>();

    public void addEntityIds(Class<?> entityType, List<String> ids) {
        createdEntityIds.computeIfAbsent(entityType, k -> new ArrayList<>()).addAll(ids);
    }

    public List<String> getEntityIds(Class<?> entityType) {
        return createdEntityIds.getOrDefault(entityType, Collections.emptyList());
    }


    public void clearAll() {
        createdEntityIds.clear();
    }
}