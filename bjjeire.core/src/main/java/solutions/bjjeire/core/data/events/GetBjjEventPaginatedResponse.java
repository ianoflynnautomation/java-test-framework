package solutions.bjjeire.api.data.events;

import solutions.bjjeire.api.data.common.PaginationMetadata;

import java.util.List;

public record GetBjjEventPaginatedResponse(
        List<BjjEvent> data,
        PaginationMetadata pagination
) {
}