package solutions.bjjeire.api.models.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ValidationErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    List<FieldError> errors) {
  @JsonCreator
  public ValidationErrorResponse(
      @JsonProperty("type") String type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("errors") List<FieldError> errors) {
    this.type = type;
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.instance = instance;
    this.errors = errors != null ? errors : List.of();
  }
}
