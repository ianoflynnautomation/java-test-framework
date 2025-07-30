package solutions.bjjeire.api.infrastructure.junit;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.api.configuration.TestConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@SpringBootTest(classes = TestConfiguration.class)
public abstract class ApiTestBase {

    private final ConcurrentLinkedQueue<Runnable> cleanupActions = new ConcurrentLinkedQueue<>();

    protected void registerForCleanup(Runnable cleanupAction) {
        cleanupActions.add(cleanupAction);
    }

    @AfterEach
    void runCleanup() {
        if (!cleanupActions.isEmpty()) {
            System.out.println("--- JUnit Teardown: Executing " + cleanupActions.size()
                    + " cleanup action(s) for thread " + Thread.currentThread().getName() + " ---");
            List<Runnable> actions = new ArrayList<>(cleanupActions);
            Collections.reverse(actions);
            actions.forEach(Runnable::run);
            cleanupActions.clear();
        }
    }
}
