package solutions.bjjeire.api.models;

public class ApiAssertionException extends AssertionError {

    public ApiAssertionException(String message, String requestPath, String responseBody) {
        super(buildDetailedMessage(message, requestPath, responseBody, null));
    }

    public ApiAssertionException(String message, String requestPath, String responseBody, Throwable cause) {
        super(buildDetailedMessage(message, requestPath, responseBody, cause), cause);
    }

    private static String buildDetailedMessage(String message, String requestPath, String responseBody,
            Throwable cause) {
        StringBuilder detailedMessage = new StringBuilder();
        detailedMessage.append("\n================ API Assertion Failed ================");
        detailedMessage.append("\n=> Failure: ").append(message);
        detailedMessage.append("\n=> Request Path: ").append(requestPath);
        detailedMessage.append("\n=> Response Body:\n").append(responseBody != null ? responseBody : "[EMPTY]");
        if (cause != null) {
            detailedMessage.append("\n=> Cause: ").append(cause.getClass().getName()).append(" - ")
                    .append(cause.getMessage());
        }
        detailedMessage.append("\n====================================================\n");
        return detailedMessage.toString();
    }
}
