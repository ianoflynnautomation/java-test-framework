package solutions.bjjeire.api.utils;

import org.slf4j.MDC;

import java.util.UUID;

public class CorrelationIdGenerator {
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final String MDC_KEY = "correlationId"; // Define MDC key for consistency

    /**
     * Generates a new UUID and sets it as the correlation ID for the current thread.
     * @return The newly generated correlation ID.
     */
    public static String generateAndSet() {
        String id = UUID.randomUUID().toString();
        CORRELATION_ID.set(id);
        MDC.put(MDC_KEY, id); // Put into MDC for Logback to pick up
        return id;
    }

    /**
     * Retrieves the correlation ID for the current thread.
     * @return The correlation ID, or null if not set.
     */
    public static String get() {
        return CORRELATION_ID.get();
    }

    /**
     * Sets the correlation ID for the current thread.
     * This method is useful if the correlation ID is generated externally
     * and needs to be propagated to the current thread's context.
     * @param id The correlation ID to set.
     */
    public static void set(String id) {
        CORRELATION_ID.set(id);
        MDC.put(MDC_KEY, id); // Also set in MDC if setting externally
    }

    /**
     * Clears the correlation ID for the current thread.
     * Important to call after each test or request to prevent ID leakage
     * and ensure proper test isolation.
     */
    public static void clear() {
        CORRELATION_ID.remove();
        MDC.remove(MDC_KEY); // Remove from MDC
    }
}