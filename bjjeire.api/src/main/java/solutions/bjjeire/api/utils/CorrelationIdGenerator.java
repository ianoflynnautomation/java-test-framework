package solutions.bjjeire.api.utils;

import java.util.UUID;

import org.slf4j.MDC;

public class CorrelationIdGenerator {
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final String MDC_KEY = "correlationId"; 


    public static String generateAndSet() {
        String id = UUID.randomUUID().toString();
        CORRELATION_ID.set(id);
        MDC.put(MDC_KEY, id); 
        return id;
    }

    public static String get() {
        return CORRELATION_ID.get();
    }


    public static void set(String id) {
        CORRELATION_ID.set(id);
        MDC.put(MDC_KEY, id); 
    }

    public static void clear() {
        CORRELATION_ID.remove();
        MDC.remove(MDC_KEY); 
    }
}