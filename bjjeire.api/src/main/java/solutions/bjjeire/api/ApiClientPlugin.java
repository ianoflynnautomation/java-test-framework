package solutions.bjjeire.api;

import solutions.bjjeire.api.models.MeasuredResponse;
import okhttp3.Interceptor;
import okhttp3.Request;
import solutions.bjjeire.api.http.ApiClient;

import java.util.Collections;
import java.util.List;

/**
 * Defines a plugin interface for extending the ApiClient's functionality.
 * Implementations can hook into the request/response lifecycle and add custom
 * OkHttp interceptors. Spring will automatically collect all beans implementing
 * this interface and inject them into the ApiClient.
 */
public interface ApiClientPlugin {

    /**
     * Called once when the ApiClient is initialized.
     * @param client The ApiClient instance.
     */
    default void onClientInitialized(ApiClient client) {}

    /**
     * Called immediately before a request is executed.
     * @param request The OkHttp Request object.
     */
    default void onMakingRequest(Request request) {}

    /**
     * Called after a request has completed, regardless of success or failure.
     * This is the primary hook for logging or metrics collection.
     * @param response The MeasuredResponse containing the result.
     */
    default void onRequestCompleted(MeasuredResponse response) {}

    /**
     * Allows plugins to add custom OkHttp Interceptors to the client.
     * These are powerful for modifying requests/responses at a low level.
     * @return A list of interceptors to be added.
     */
    default List<Interceptor> getInterceptors() {
        return Collections.emptyList();
    }
}