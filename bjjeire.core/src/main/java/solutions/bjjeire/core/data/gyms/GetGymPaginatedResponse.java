package solutions.bjjeire.core.data.gyms;

import solutions.bjjeire.core.data.common.PaginationMetadata;

import java.util.List;

public record GetGymPaginatedResponse(
        List<Gym> data,
        PaginationMetadata pagination
) {
}
