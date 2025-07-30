package solutions.bjjeire.api.models;


/**
 * Custom exception for assertion failures in API tests.
 * This provides more context than a standard assertion error, including the request path and response body,
 * which is invaluable for debugging failures against a staging environment.
 */
public class ApiAssertionException extends AssertionError {

    /**
     * Constructs an ApiAssertionException with a detailed error message.
     *
     * @param message     The primary assertion failure message.
     * @param requestPath The path of the API request that failed.
     * @param responseBody The body of the response, if available.
     */
    public ApiAssertionException(String message, String requestPath, String responseBody) {
        super(buildDetailedMessage(message, requestPath, responseBody, null));
    }

    /**
     * Constructs an ApiAssertionException with a detailed error message and a cause.
     *
     * @param message      The primary assertion failure message.
     * @param requestPath  The path of the API request that failed.
     * @param responseBody The body of the response, if available.
     * @param cause        The underlying exception that caused the failure.
     */
    public ApiAssertionException(String message, String requestPath, String responseBody, Throwable cause) {
        super(buildDetailedMessage(message, requestPath, responseBody, cause), cause);
    }

    /**
     * Helper method to build a structured and informative error message.
     */
    private static String buildDetailedMessage(String message, String requestPath, String responseBody, Throwable cause) {
        StringBuilder detailedMessage = new StringBuilder();
        detailedMessage.append("\n================ API Assertion Failed ================");
        detailedMessage.append("\n=> Failure: ").append(message);
        detailedMessage.append("\n=> Request Path: ").append(requestPath);
        detailedMessage.append("\n=> Response Body:\n").append(responseBody != null ? responseBody : "[EMPTY]");
        if (cause != null) {
            detailedMessage.append("\n=> Cause: ").append(cause.getClass().getName()).append(" - ").append(cause.getMessage());
        }
        detailedMessage.append("\n====================================================\n");
        return detailedMessage.toString();
    }
}
