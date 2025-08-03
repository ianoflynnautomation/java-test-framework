package solutions.bjjeire.api.utils;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

public class CorrelationIdFilter {
    public ExchangeFilterFunction getFilter() {
        return (clientRequest, next) -> {
            String correlationId = CorrelationIdGenerator.get();
            if (correlationId == null) {
                correlationId = CorrelationIdGenerator.generateAndSet();
            }

            ClientRequest newRequest = ClientRequest.from(clientRequest)
                    .header("X-Correlation-ID", correlationId)
                    .build();
            return next.exchange(newRequest);
        };
    }
}