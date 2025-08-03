package solutions.bjjeire.api.models;

public class ApiAssertionException extends AssertionError {
    private final String requestPath;
    private final String responseBody;

    public ApiAssertionException(String message, String requestPath, String responseBody) {
        super(message + "\nRequest Path: " + requestPath + "\nResponse Body: " + responseBody);
        this.requestPath = requestPath;
        this.responseBody = responseBody;
    }

    public ApiAssertionException(String message, String requestPath, String responseBody, Throwable cause) {
        super(message + "\nRequest Path: " + requestPath + "\nResponse Body: " + responseBody, cause);
        this.requestPath = requestPath;
        this.responseBody = responseBody;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getResponseBody() {
        return responseBody;
    }
}