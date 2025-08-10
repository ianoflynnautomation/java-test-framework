package solutions.bjjeire.selenium.web.data.strategy;

import org.springframework.stereotype.Component;

import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

@Component
public class EventApiStrategy implements EntityApiStrategy<BjjEvent, CreateBjjEventCommand, CreateBjjEventResponse> {
    @Override
    public Class<BjjEvent> getEntityType() {
        return BjjEvent.class;
    }

    @Override
    public String getApiPath() {
        return "/api/bjjevent";
    }

    @Override
    public String getEntityName(BjjEvent event) {
        return event.name();
    }

    @Override
    public CreateBjjEventCommand createCommand(BjjEvent event) {
        return new CreateBjjEventCommand(event);
    }

    @Override
    public Class<CreateBjjEventResponse> getResponseClass() {
        return CreateBjjEventResponse.class;
    }

    @Override
    public String getIdFromResponse(CreateBjjEventResponse response) {
        return response.data().id();
    }
}