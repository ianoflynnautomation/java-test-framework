package solutions.bjjeire.api.client;

import reactor.core.publisher.Mono;
import solutions.bjjeire.api.validation.ApiResponse;

public abstract class BaseApiClient {

  protected final RequestExecutor requestExecutor;

  protected BaseApiClient(RequestExecutor requestExecutor) {
    this.requestExecutor = requestExecutor;
  }

  protected Mono<ApiResponse> execute(ApiRequest request) {
    return requestExecutor.execute(request);
  }
}
