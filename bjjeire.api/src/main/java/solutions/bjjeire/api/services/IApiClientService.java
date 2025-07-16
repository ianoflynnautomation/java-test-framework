package solutions.bjjeire.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import solutions.bjjeire.api.models.MeasuredResponse;

public interface IApiClientService extends AutoCloseable {

    /**
     * Executes a given OkHttp Request and deserializes the response.
     *
     * @param request The fully constructed request to execute.
     * @param responseType The class to which the successful response body should be deserialized.
     * @param <T> The type of the response object.
     * @return A MeasuredResponse containing the deserialized data, status code, and execution time.
     */
    <T> MeasuredResponse<T> execute(Request request, Class<T> responseType);

    ObjectMapper getObjectMapper();

    /**
     * Closes underlying resources, such as the HTTP client's connection pool.
     */
    @Override
    void close();
}
