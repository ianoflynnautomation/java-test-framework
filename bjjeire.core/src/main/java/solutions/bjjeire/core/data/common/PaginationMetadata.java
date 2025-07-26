package solutions.bjjeire.api.data.common;

/**
 * A record representing the metadata for a paginated response.
 * Corresponds to the C# PaginationMetadata.
 */
public record PaginationMetadata(
        int totalItems,
        int currentPage,
        int pageSize,
        int totalPages,
        boolean hasNextPage,
        boolean hasPreviousPage,
        String nextPageUrl,
        String previousPageUrl
) {
}