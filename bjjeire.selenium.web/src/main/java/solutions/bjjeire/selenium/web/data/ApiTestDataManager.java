
package solutions.bjjeire.selenium.web.data;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import reactor.core.publisher.Flux;
import solutions.bjjeire.api.auth.BearerTokenAuth;
import solutions.bjjeire.api.client.BjjEventsApiClient;
import solutions.bjjeire.api.client.GymsApiClient;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

@Service
@Slf4j
@Primary
@Profile("development")
@RequiredArgsConstructor
public class ApiTestDataManager implements TestDataManager {

    private final AuthService authService;
    private final BjjEventsApiClient eventsApiClient;
    private final GymsApiClient gymsApiClient;

    @Override
    public String authenticate() {
        log.info("Authenticating user for UI test data setup via API framework");
        return authService.getTokenFor("admin").block(Duration.ofSeconds(10));
    }

    @Override
    public <T> List<String> seed(List<T> entities, String authToken) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Seeding {} entities for UI test", entities.size(),
                StructuredArguments.keyValue("entityType", entities.get(0).getClass().getSimpleName()));

        BearerTokenAuth auth = new BearerTokenAuth(authToken);

        return Flux.fromIterable(entities)
                .flatMap(entity -> {
                    if (entity instanceof BjjEvent event) {
                        return eventsApiClient.createEvent(auth, new CreateBjjEventCommand(event))
                                .map(response -> response.as(CreateBjjEventResponse.class).data().id());
                    }
                    if (entity instanceof Gym gym) {
                        return gymsApiClient.createGym(auth, new CreateGymCommand(gym))
                                .map(response -> response.as(CreateGymResponse.class).data().id());
                    }
                    return Flux.error(
                            new IllegalArgumentException("Unsupported entity type for seeding: " + entity.getClass()));
                })
                .collectList()
                .block(Duration.ofSeconds(30));
    }

    @Override
    public <T> void teardown(Class<T> entityType, List<String> ids, String authToken) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        log.info("Tearing down {} entities for UI test", ids.size(),
                StructuredArguments.keyValue("entityType", entityType.getSimpleName()));

        BearerTokenAuth auth = new BearerTokenAuth(authToken);

        var teardownFlux = Flux.fromIterable(ids)
                .flatMap(id -> {
                    if (BjjEvent.class.equals(entityType)) {
                        return eventsApiClient.deleteEvent(auth, id);
                    }
                    if (Gym.class.equals(entityType)) {
                        return gymsApiClient.deleteGym(auth, id);
                    }
                    return Flux
                            .error(new IllegalArgumentException("Unsupported entity type for teardown: " + entityType));
                });

        teardownFlux.then().block(Duration.ofSeconds(30));
        log.info("Teardown complete for {}", entityType.getSimpleName());
    }
}