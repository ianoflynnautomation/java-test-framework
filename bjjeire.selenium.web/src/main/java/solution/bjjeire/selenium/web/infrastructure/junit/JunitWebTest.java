package solution.bjjeire.selenium.web.infrastructure.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import solution.bjjeire.selenium.web.infrastructure.BrowserLifecyclePlugin;
import solution.bjjeire.selenium.web.services.App;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;
import solutions.bjjeire.core.plugins.junit.TestResultWatcher;
import solutions.bjjeire.core.utilities.SingletonFactory;

@ExtendWith(TestResultWatcher.class)
public class JunitWebTest extends JunitBaseTest {

    public App app() {
        return SingletonFactory.getInstance(App.class);
    }

    @Override
    protected void configure() {
        addPlugin(BrowserLifecyclePlugin.class);
    }
}