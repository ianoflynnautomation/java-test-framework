package solutions.bjjeire.api.models;


public class ApiAssertionException extends AssertionError {
    public ApiAssertionException(String message, String requestUrl, String responseBody, Throwable cause) {
        super(String.format("%s\nRequest URL: %s\nResponse Body: \n%s", message, requestUrl, responseBody), cause);
    }
    public ApiAssertionException(String message, String requestUrl, String responseBody) {
        this(message, requestUrl, responseBody, null);
    }
}
