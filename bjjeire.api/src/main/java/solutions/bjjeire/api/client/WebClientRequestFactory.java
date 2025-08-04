package solutions.bjjeire.api.client;

import org.springframework.web.reactive.function.client.WebClient;
import solutions.bjjeire.api.http.auth.Authentication;

public class WebClientRequestFactory {
    private final WebClient webClient;
    private final RequestBodyHandler bodyHandler;

    public WebClientRequestFactory(WebClient webClient, RequestBodyHandler bodyHandler) {
        this.webClient = webClient;
        this.bodyHandler = bodyHandler;
    }

    public WebClient.RequestHeadersSpec<?> create(ApiRequest request) {
        WebClient.RequestBodySpec requestBodySpec = webClient
                .method(request.getMethod())
                .uri(uriBuilder -> uriBuilder.path(request.getPath()).queryParams(request.getQueryParams()).build())
                .headers(headers -> {
                    headers.addAll(request.getHeaders());
                    Authentication authentication = request.getAuthentication();
                    if (authentication != null) {
                        authentication.apply(headers);
                    }
                });

        return bodyHandler.handleBody(requestBodySpec, request);
    }
}