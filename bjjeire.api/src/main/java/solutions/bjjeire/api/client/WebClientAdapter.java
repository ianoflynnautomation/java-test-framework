package solutions.bjjeire.api.client;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.configuration.WebClientConfig;
import solutions.bjjeire.api.validation.ApiResponse;


@Component
public class WebClientAdapter implements Client {
    private final RequestExecutor requestExecutor;

    public WebClientAdapter(ApiSettings settings, WebClientConfig webClientConfig,
                            RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    @Override
    public Mono<ApiResponse> execute(ApiRequest request) {
        return requestExecutor.execute(request);
    }
}