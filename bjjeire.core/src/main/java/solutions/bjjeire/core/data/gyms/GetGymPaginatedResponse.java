package solutions.bjjeire.core.data.gyms;

import java.util.List;
import solutions.bjjeire.core.data.common.PaginationMetadata;

public record GetGymPaginatedResponse(List<Gym> data, PaginationMetadata pagination) {}
