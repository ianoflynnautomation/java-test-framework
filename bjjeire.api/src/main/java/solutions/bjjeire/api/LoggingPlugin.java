package solutions.bjjeire.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.models.MeasuredResponse;
import okhttp3.Request;

/**
 * A default plugin that logs API requests and responses using SLF4J.
 * As a Spring @Component, it is automatically detected and registered with the ApiClient.
 */
@Component
public class LoggingPlugin implements ApiClientPlugin {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPlugin.class);

    @Override
    public void onMakingRequest(Request request) {
        logger.info("--> {} {}", request.method(), request.url());
    }

    @Override
    public void onRequestCompleted(MeasuredResponse response) {
        if (response.rawResponse().isSuccessful()) {
            logger.info("<-- {} {} ({}ms)",
                    response.statusCode(),
                    response.requestUrl(),
                    response.executionTime().toMillis());
        } else {
            logger.error("<-- FAILED: {} {} ({}ms)\nResponse Body:\n{}",
                    response.statusCode(),
                    response.requestUrl(),
                    response.executionTime().toMillis(),
                    response.responseBodyAsString());
        }
    }
}