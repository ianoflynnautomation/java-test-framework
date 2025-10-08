package solutions.bjjeire.api.exceptions;

public class ApiResponseDeserializationException extends ApiException {

  private final String responseBody;
  private final String targetType;

  public ApiResponseDeserializationException(
      String message, String responseBody, String targetType, Throwable cause) {
    super(buildMessage(message, responseBody, targetType), cause);
    this.responseBody = responseBody;
    this.targetType = targetType;
  }

  private static String buildMessage(String originalMessage, String body, String type) {
    return String.format(
        "Failed to deserialize response body to type '%s'. Error: %s%n"
            + "Response Body:%n"
            + "--------------%n"
            + "%s",
        type, originalMessage, body);
  }

  public String getResponseBody() {
    return responseBody;
  }

  public String getTargetType() {
    return targetType;
  }
}
