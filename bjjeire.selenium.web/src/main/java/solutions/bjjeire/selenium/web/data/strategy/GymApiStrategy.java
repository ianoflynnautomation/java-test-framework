package solutions.bjjeire.selenium.web.data.strategy;

import org.springframework.stereotype.Component;

import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

@Component
public class GymApiStrategy implements EntityApiStrategy<Gym, CreateGymCommand, CreateGymResponse> {
    @Override
    public Class<Gym> getEntityType() {
        return Gym.class;
    }

    @Override
    public String getApiPath() {
        return "/api/gym";
    }

    @Override
    public String getEntityName(Gym gym) {
        return gym.name();
    }

    @Override
    public CreateGymCommand createCommand(Gym gym) {
        return new CreateGymCommand(gym);
    }

    @Override
    public Class<CreateGymResponse> getResponseClass() {
        return CreateGymResponse.class;
    }

    @Override
    public String getIdFromResponse(CreateGymResponse response) {
        return response.data().id();
    }
}