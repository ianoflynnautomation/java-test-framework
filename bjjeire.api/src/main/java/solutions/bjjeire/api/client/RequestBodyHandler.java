package solutions.bjjeire.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
public class RequestBodyHandler {

    private final ObjectMapper objectMapper;

    public RequestBodyHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public WebClient.RequestHeadersSpec<?> handleBody(WebClient.RequestBodySpec requestBodySpec, ApiRequest request) {
        if (request.getBody() == null) {
            return requestBodySpec;
        }

        if (request.getContentType() != null && request.getContentType().equals(MediaType.MULTIPART_FORM_DATA)) {
            if (!(request.getBody() instanceof MultiValueMap)) {
                throw new IllegalArgumentException("For MULTIPART_FORM_DATA, the body must be a MultiValueMap<String, Object>");
            }
            return requestBodySpec.body(BodyInserters.fromMultipartData((MultiValueMap<String, ?>) request.getBody()));
        } else if (request.getContentType() != null && request.getContentType().equals(MediaType.APPLICATION_FORM_URLENCODED)) {
            if (!(request.getBody() instanceof MultiValueMap)) {
                throw new IllegalArgumentException("For APPLICATION_FORM_URLENCODED, the body must be a MultiValueMap<String, String>");
            }
            return requestBodySpec.body(BodyInserters.fromFormData((MultiValueMap<String, String>) request.getBody()));
        } else {
            return requestBodySpec.contentType(request.getContentType()).bodyValue(request.getBody());
        }
    }

}