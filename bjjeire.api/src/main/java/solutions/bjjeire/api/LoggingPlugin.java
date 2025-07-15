package solutions.bjjeire.api;

import solutions.bjjeire.api.models.MeasuredResponse;
import okhttp3.Request;

public class LoggingPlugin implements ApiClientPlugin {
    @Override
    public void onMakingRequest(Request request) {
        System.out.printf("--> %s %s%n", request.method(), request.url());
    }

    @Override
    public void onRequestMade(MeasuredResponse<?> response) {
        System.out.printf("<-- %d %s (%d ms)%n", response.getStatusCode(), response.getRawResponse().request().url(), response.getExecutionTime().toMillis());
    }

    @Override
    public void onRequestFailed(MeasuredResponse<?> response) {
        System.err.printf("!!! FAILED REQUEST: %d %s%nBody: %s%n",
                response.getStatusCode(),
                response.getRawResponse().request().url(),
                response.getResponseBodyAsString());
    }
}