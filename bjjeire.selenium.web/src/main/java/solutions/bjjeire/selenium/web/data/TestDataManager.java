package solutions.bjjeire.selenium.web.data;

import solutions.bjjeire.core.data.events.BjjEvent;

import java.util.List;

public interface TestDataManager {
    String authenticate();
    List<String> seedEvents(List<BjjEvent> events, String authToken);
    void teardown(List<String> createdEventIds, String authToken);
}