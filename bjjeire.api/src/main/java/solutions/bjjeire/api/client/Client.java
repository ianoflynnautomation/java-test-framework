package solutions.bjjeire.api.client;

import reactor.core.publisher.Mono;
import solutions.bjjeire.api.validation.ApiResponse;

public interface Client {
  Mono<ApiResponse> execute(ApiRequest request);
}
