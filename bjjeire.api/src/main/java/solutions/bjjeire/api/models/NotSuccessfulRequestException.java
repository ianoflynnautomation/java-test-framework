package solutions.bjjeire.api.models;

import java.io.IOException;

public class NotSuccessfulRequestException extends IOException {
    public NotSuccessfulRequestException(String message) {
        super(message);
    }
}