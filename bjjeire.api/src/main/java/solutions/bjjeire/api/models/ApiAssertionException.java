package solutions.bjjeire.api.models;

public class ApiAssertionException extends AssertionError {
    public ApiAssertionException(String message, String url, String responseBody) {
        super(formatExceptionMessage(message, url, responseBody));
    }

    public ApiAssertionException(String message, String url, String responseBody, Throwable cause) {
        super(formatExceptionMessage(message, url, responseBody), cause);
    }

    private static String formatExceptionMessage(String message, String url, String responseBody) {
        String separator = "\n" + "#".repeat(80) + "\n";
        return String.format(
                "%sAPI Assertion Failed!\n\nMessage: %s\nURL: %s\n\nResponse Body:\n---\n%s\n---%s",
                separator, message, url, responseBody, separator
        );
    }
}