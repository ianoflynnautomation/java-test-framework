package solutions.bjjeire.selenium.web.data;

import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.List;

public interface TestDataManager {
    String authenticate();

    List<String> seedEvents(List<BjjEvent> events, String authToken);

    List<String> seedGyms(List<Gym> gyms, String authToken);

    void teardownEvents(List<String> createdEventIds, String authToken);

    void teardownGyms(List<String> createdGymIds, String authToken);
}