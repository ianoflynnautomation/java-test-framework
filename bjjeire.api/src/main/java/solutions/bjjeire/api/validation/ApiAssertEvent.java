package solutions.bjjeire.api.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.models.MeasuredResponse;

@Getter
@RequiredArgsConstructor
public class ApiAssertEvent {
    private final MeasuredResponse<?> response;
    private final String assertionType;
    private final String expectedValue;
}
