package solutions.bjjeire.core.data.events;

import java.util.List;

import solutions.bjjeire.core.data.common.PaginationMetadata;

public record GetBjjEventPaginatedResponse(
                List<BjjEvent> data,
                PaginationMetadata pagination) {
}