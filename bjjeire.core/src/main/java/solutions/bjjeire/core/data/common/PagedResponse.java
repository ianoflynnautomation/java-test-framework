package solutions.bjjeire.core.data.common;

import java.util.List;

/**
 * A generic record for a paginated response, containing a list of data items
 * and pagination metadata.
 * Corresponds to the C# PagedResponse<T>.
 *
 * @param <T> The type of the data items in the list.
 */
public record PagedResponse<T>(
        List<T> data,
        PaginationMetadata pagination
) {
}
