package solutions.bjjeire.api.data.events;

import solutions.bjjeire.api.data.common.PaginationMetadata;

import java.util.List;

/**
 * A specific paginated response record for BJJ events.
 * Corresponds to the C# GetBjjEventPaginatedResponse.
 */
public record GetBjjEventPaginatedResponse(
        List<BjjEvent> data,
        PaginationMetadata pagination
) {
}