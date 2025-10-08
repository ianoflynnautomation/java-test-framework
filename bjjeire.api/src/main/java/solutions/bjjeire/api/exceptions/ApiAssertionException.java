package solutions.bjjeire.api.exceptions;

import io.github.resilience4j.core.lang.Nullable;

public class ApiAssertionException extends AssertionError {
  private final String requestPath;
  private final String responseBody;

  public ApiAssertionException(String message, String requestPath, @Nullable String responseBody) {
    super(buildAssertionMessage(message, requestPath, responseBody));
    this.requestPath = requestPath;
    this.responseBody = responseBody;
  }

  public ApiAssertionException(
      String message, String requestPath, @Nullable String responseBody, Throwable cause) {
    super(buildAssertionMessage(message, requestPath, responseBody), cause);
    this.requestPath = requestPath;
    this.responseBody = responseBody;
  }

  private static String buildAssertionMessage(
      String message, String requestPath, @Nullable String responseBody) {
    return String.format(
        "%s%n"
            + "================ API Assertion Details ===============%n"
            + "  Request Path:  %s%n"
            + "  Response Body:%n"
            + "  --------------%n"
            + "  %s%n"
            + "======================================================",
        message, requestPath, responseBody != null ? responseBody : "[Empty or Null]");
  }

  public String getRequestPath() {
    return requestPath;
  }

  public String getResponseBody() {
    return responseBody;
  }
}
