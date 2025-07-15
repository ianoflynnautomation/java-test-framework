package solutions.bjjeire.api;

import solutions.bjjeire.api.models.MeasuredResponse;
import okhttp3.Interceptor;
import okhttp3.Request;
import solutions.bjjeire.api.services.ApiClientService;

import java.util.List;

interface ApiClientPlugin {
    default void onClientInitialized(ApiClientService service) {}
    default void onMakingRequest(Request request) {}
    default void onRequestMade(MeasuredResponse<?> response) {}
    default void onRequestFailed(MeasuredResponse<?> response) {}
    default List<Interceptor> getInterceptors() {
        return List.of();
    }
}