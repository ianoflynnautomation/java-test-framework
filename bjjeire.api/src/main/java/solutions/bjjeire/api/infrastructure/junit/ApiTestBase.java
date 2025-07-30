package solutions.bjjeire.api.infrastructure.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.api.configuration.TestConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Spring-enabled base class for all API test classes.
 * It uses @SpringBootTest to load the full application context defined by
 * TestConfiguration.class, making all Spring beans available for injection.
 * It also provides a convenient factory method for creating isolated TestClient instances.
 */
@SpringBootTest(classes = TestConfiguration.class)
public abstract class ApiTestBase {

    private final ConcurrentLinkedQueue<Runnable> cleanupActions = new ConcurrentLinkedQueue<>();

    /**
     * Registers a cleanup action (e.g., a resource deletion lambda) to be executed
     * automatically after the test completes.
     * @param cleanupAction The Runnable action to execute.
     */
    protected void registerForCleanup(Runnable cleanupAction) {
        cleanupActions.add(cleanupAction);
    }

    @AfterEach
    void runCleanup() {
        if (!cleanupActions.isEmpty()) {
            System.out.println("--- JUnit Teardown: Executing " + cleanupActions.size() + " cleanup action(s) for thread " + Thread.currentThread().getName() + " ---");
            List<Runnable> actions = new ArrayList<>(cleanupActions);
            Collections.reverse(actions);
            actions.forEach(Runnable::run);
            cleanupActions.clear();
        }
    }
}
