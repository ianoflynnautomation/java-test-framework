package solutions.bjjeire.api.infrastructure.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.configuration.TestConfiguration;
import solutions.bjjeire.api.utils.TestLifecycleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@SpringBootTest(classes = TestConfiguration.class)
@ExtendWith(TestLifecycleLogger.class)
public abstract class ApiTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeAll
    static void setup(@Autowired ApplicationContext applicationContext) {
        TestLifecycleLogger.SpringContext.setApplicationContext(applicationContext);
    }

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
