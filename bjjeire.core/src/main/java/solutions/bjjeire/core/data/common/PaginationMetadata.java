package solutions.bjjeire.core.data.common;

public record PaginationMetadata(
    int totalItems,
    int currentPage,
    int pageSize,
    int totalPages,
    boolean hasNextPage,
    boolean hasPreviousPage,
    String nextPageUrl,
    String previousPageUrl) {}
