package solutions.bjjeire.api.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.validation.ApiResponse;


@Component
@RequiredArgsConstructor
public class WebClientAdapter implements Client {

    private final RequestExecutor requestExecutor;

    @Override
    public Mono<ApiResponse> execute(ApiRequestBuilder request) {
        return requestExecutor.execute(request);
    }
}