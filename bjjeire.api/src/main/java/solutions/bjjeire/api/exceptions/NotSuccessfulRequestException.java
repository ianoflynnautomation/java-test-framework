package solutions.bjjeire.api.exceptions;

import java.io.IOException;

public class NotSuccessfulRequestException extends IOException {
  public NotSuccessfulRequestException(String message) {
    super(message);
  }
}
