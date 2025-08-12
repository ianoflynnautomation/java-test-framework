package solutions.bjjeire.core.data.common;

import java.util.List;

public record PagedResponse<T>(
                List<T> data,
                PaginationMetadata pagination) {
}
