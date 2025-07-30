package solutions.bjjeire.api.models.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FieldError(String field, String message, String errorCode) {
    @JsonCreator
    public FieldError(
            @JsonProperty("field") String field,
            @JsonProperty("message") String message,
            @JsonProperty("errorCode") String errorCode) {
        this.field = field;
        this.message = message;
        this.errorCode = errorCode;
    }
}