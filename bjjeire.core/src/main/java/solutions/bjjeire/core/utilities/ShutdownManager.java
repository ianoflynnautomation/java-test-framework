package solutions.bjjeire.core.utilities;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShutdownManager {
  private static final Logger log = LoggerFactory.getLogger(ShutdownManager.class);
  private final List<Runnable> instructions = new CopyOnWriteArrayList<>();
  private final ExecutorService executor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
      executor.submit(
          () -> {
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

      if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();

        if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
          log.error("Executor service did not terminate.");
        }
      }
    } catch (InterruptedException ie) {

      pool.shutdownNow();

      Thread.currentThread().interrupt();
    }
  }
}
