package solutions.bjjeire.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.exceptions.ApiRequestException;
import solutions.bjjeire.api.utils.RetryPolicy;
import solutions.bjjeire.api.validation.ApiResponse;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestExecutor {

    private final WebClient webClient;
    private final RetryPolicy retryPolicy;
    private final ObjectMapper objectMapper;

    public Mono<ApiResponse> execute(ApiRequestBuilder request) {
        long startTime = System.nanoTime();

        return createRequestSpec(request)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)
                        .map(responseEntity -> new ApiResponse(
                                responseEntity,
                                Duration.ofNanos(System.nanoTime() - startTime),
                                objectMapper,
                                request.getPath())))
                .doOnSuccess(this::logApiInteraction)
                .doOnError(error -> logApiFailure(request, error))
                .retryWhen(retryPolicy.getRetrySpec(request.getPath(), request.getMethod()));
    }

    private WebClient.RequestHeadersSpec<?> createRequestSpec(ApiRequestBuilder request) {
        WebClient.RequestBodySpec requestBodySpec = webClient
                .method(request.getMethod())
                .uri(uriBuilder -> uriBuilder.path(request.getPath()).queryParams(request.getQueryParams()).build())
                .headers(headers -> {
                    headers.addAll(request.getHeaders());
                    if (request.getAuthentication() != null) {
                        request.getAuthentication().apply(headers);
                    }
                })
                .accept(request.getAcceptableMediaTypes().toArray(new MediaType[0]));

        return handleBody(requestBodySpec, request);
    }

    @SuppressWarnings("unchecked")
    private WebClient.RequestHeadersSpec<?> handleBody(WebClient.RequestBodySpec requestBodySpec,
            ApiRequestBuilder request) {
        if (request.getBody() == null) {
            return requestBodySpec;
        }

        MediaType contentType = request.getContentType();
        Object body = request.getBody();

        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            if (!(body instanceof MultiValueMap)) {

                throw new ApiRequestException("For MULTIPART_FORM_DATA, the body must be a MultiValueMap.");
            }
            return requestBodySpec.body(BodyInserters.fromMultipartData((MultiValueMap<String, ?>) body));
        }

        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
            if (!(body instanceof MultiValueMap)) {

                throw new ApiRequestException(
                        "For APPLICATION_FORM_URLENCODED, the body must be a MultiValueMap.");
            }
            return requestBodySpec.body(BodyInserters.fromFormData((MultiValueMap<String, String>) body));
        }

        return requestBodySpec.contentType(contentType).bodyValue(body);
    }

    private void logApiInteraction(ApiResponse response) {
        log.info("API Interaction",
                StructuredArguments.kv("eventType", "api_interaction"),
                StructuredArguments.kv("url", response.getRequestPath()),
                StructuredArguments.kv("response_status_code", response.getStatusCode()),
                StructuredArguments.kv("response_body", truncateBody(response.getBodyAsString(), 1000)),
                StructuredArguments.kv("duration_ms", response.getExecutionTime().toMillis()));
    }

    private void logApiFailure(ApiRequestBuilder request, Throwable error) {
        log.error("API Request Execution Failed",
                StructuredArguments.kv("eventType", "api_failure"),
                StructuredArguments.kv("url", request.getPath()),
                StructuredArguments.kv("method", request.getMethod().name()),
                StructuredArguments.kv("error", error.getMessage()),
                error);
    }

    private String truncateBody(String body, int maxLength) {
        if (body == null)
            return null;
        return body.length() > maxLength ? body.substring(0, maxLength) + "..." : body;
    }
}
