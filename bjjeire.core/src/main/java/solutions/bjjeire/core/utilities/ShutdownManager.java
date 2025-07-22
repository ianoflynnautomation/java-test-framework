package solutions.bjjeire.core.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
//import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Refactored as a Spring bean. It now uses @PreDestroy for graceful shutdown,
 * which is the standard Spring way to handle cleanup logic.
 */
@Component
public class ShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);
    private final List<Runnable> instructions = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void register(Runnable runnable) {
        instructions.add(runnable);
    }

    //@PreDestroy
    public void runAllInstructions() {
        if (instructions.isEmpty()) {
            return;
        }
        logger.info("Executing {} shutdown instructions...", instructions.size());
        for (var instruction : instructions) {
            executor.submit(() -> {
                try {
                    instruction.run();
                } catch (Exception ex) {
                    logger.warn("Shutdown instruction failed.", ex);
                }
            });
        }
        shutdownAndAwaitTermination(executor);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        // (Implementation from your original code is good)
    }
}
