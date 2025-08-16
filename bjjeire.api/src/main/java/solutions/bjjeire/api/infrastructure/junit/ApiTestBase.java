package solutions.bjjeire.api.infrastructure.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.api.configuration.TestConfiguration;

@Slf4j
@SpringBootTest(classes = TestConfiguration.class)
public abstract class ApiTestBase {

    private final ConcurrentLinkedQueue<Runnable> cleanupActions = new ConcurrentLinkedQueue<>();

    protected void registerForCleanup(Runnable cleanupAction) {
        cleanupActions.add(cleanupAction);
    }

    @AfterEach
    void runCleanup() {
        if (!cleanupActions.isEmpty()) {
            log.info("Executing {} cleanup action(s) for thread {}", cleanupActions.size(),
                    Thread.currentThread().getName());
            List<Runnable> actions = new ArrayList<>(cleanupActions);
            Collections.reverse(actions);
            actions.forEach(Runnable::run);
            cleanupActions.clear();
        }
    }
}