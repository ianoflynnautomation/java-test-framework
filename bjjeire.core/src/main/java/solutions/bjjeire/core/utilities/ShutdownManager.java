package solutions.bjjeire.core.utilities;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class ShutdownManager {
    private static final List<Runnable> instructions = new CopyOnWriteArrayList<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ShutdownManager::runAllInstructions));
    }

    public static void register(Runnable runnable) {
        instructions.add(runnable);
    }

    private static void runAllInstructions() {
        if (instructions.isEmpty()) return;

        try {
            // Submit all instructions for parallel execution
            for (var instruction : instructions) {
                executor.submit(() -> {
                    try {
                        instruction.run();
                    } catch (Exception ex) {
                        //DebugInformation.debugInfo(ex.getMessage());
                    }
                });
            }
        } finally {
            shutdownAndAwaitTermination(executor);
        }
    }

    private static void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}