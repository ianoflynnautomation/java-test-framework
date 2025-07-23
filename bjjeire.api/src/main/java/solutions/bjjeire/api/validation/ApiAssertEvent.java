package solutions.bjjeire.api.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.models.MeasuredResponse;

public record ApiAssertEvent(
        MeasuredResponse response,
        String assertionType,
        Object expectedValue,
        Object actualValue,
        boolean success
) {
}
