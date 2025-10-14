package solutions.bjjeire.api.client;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.endpoints.BjjEventEndpoints;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;

@Component
public class BjjEventsApiClient extends BaseApiClient {

  protected BjjEventsApiClient(RequestExecutor requestExecutor) {
    super(requestExecutor);
  }

  public Mono<ApiResponse> createEvent(Authentication auth, CreateBjjEventCommand command) {
    return execute(
        ApiRequest.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .authentication(auth)
            .body(command)
            .build());
  }

  public Mono<ApiResponse> createEventWithInvalidPayload(
      Authentication auth, Object invalidPayload) {
    return execute(
        ApiRequest.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .authentication(auth)
            .body(invalidPayload)
            .build());
  }

  public Mono<ApiResponse> deleteEvent(Authentication auth, String eventId) {
    return execute(
        ApiRequest.builder()
            .delete(BjjEventEndpoints.bjjEventById(eventId))
            .authentication(auth)
            .build());
  }
}
