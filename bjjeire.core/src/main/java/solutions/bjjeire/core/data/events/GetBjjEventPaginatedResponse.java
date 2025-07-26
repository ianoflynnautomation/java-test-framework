package solutions.bjjeire.core.data.events;


import solutions.bjjeire.core.data.common.PaginationMetadata;

import java.util.List;

public record GetBjjEventPaginatedResponse(
        List<BjjEvent> data,
        PaginationMetadata pagination
) {
}