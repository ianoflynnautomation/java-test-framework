package solutions.bjjeire.core.utilities;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
//import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages graceful shutdown of resources. As a Spring bean, it leverages
 * the @PreDestroy annotation to automatically run cleanup tasks when the
 * Spring context is closed.
 */
@Component
public class ShutdownManager {
    private static final Logger log = LoggerFactory.getLogger(ShutdownManager.class);
    private final List<Runnable> instructions = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void register(Runnable runnable) {
        instructions.add(runnable);
    }

    @PreDestroy
    public void runAllInstructions() {
        if (instructions.isEmpty()) {
            return;
        }
        log.info("Executing {} shutdown instructions...", instructions.size());
        for (var instruction : instructions) {
            executor.submit(() -> {
                try {
                    instruction.run();
                } catch (Exception ex) {
                    log.warn("Shutdown instruction failed.", ex);
                }
            });
        }
        shutdownAndAwaitTermination(executor);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Executor service did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
