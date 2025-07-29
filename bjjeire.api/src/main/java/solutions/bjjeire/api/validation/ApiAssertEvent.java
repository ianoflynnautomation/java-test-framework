package solutions.bjjeire.api.validation;

import solutions.bjjeire.api.models.MeasuredResponse;

public record ApiAssertEvent(
        MeasuredResponse response,
        String assertionType,
        Object expectedValue,
        Object actualValue,
        boolean success
) {
}
