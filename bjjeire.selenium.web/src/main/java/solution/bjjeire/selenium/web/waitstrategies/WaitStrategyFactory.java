package solution.bjjeire.selenium.web.waitstrategies;

import solution.bjjeire.selenium.web.configuration.TimeoutSettings;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.configuration.ConfigurationService;

public class WaitStrategyFactory {
    private final TimeoutSettings timeoutSettings;

    public WaitStrategyFactory() {
        timeoutSettings = ConfigurationService.get(WebSettings.class).getTimeoutSettings();
    }

    public ToExistWaitStrategy exist() {
        return new ToExistWaitStrategy(timeoutSettings.getElementToExistTimeout(), timeoutSettings.getSleepInterval());
    }

    public ToExistWaitStrategy exist(long timeoutInterval, long sleepInterval) {
        return new ToExistWaitStrategy(timeoutInterval, sleepInterval);
    }

    public ToBeVisibleWaitStrategy beVisible(long timeoutInterval, long sleepInterval) {
        return new ToBeVisibleWaitStrategy(timeoutInterval, sleepInterval);
    }

    public ToBeVisibleWaitStrategy beVisible() {
        return new ToBeVisibleWaitStrategy(timeoutSettings.getElementToBeVisibleTimeout(), timeoutSettings.getSleepInterval());
    }

    public ToBeClickableWaitStrategy beClickable(long timeoutInterval, long sleepInterval) {
        return new ToBeClickableWaitStrategy(timeoutInterval, sleepInterval);
    }

    public ToBeClickableWaitStrategy beClickable() {
        return new ToBeClickableWaitStrategy(timeoutSettings.getElementToBeClickableTimeout(), timeoutSettings.getSleepInterval());
    }



}