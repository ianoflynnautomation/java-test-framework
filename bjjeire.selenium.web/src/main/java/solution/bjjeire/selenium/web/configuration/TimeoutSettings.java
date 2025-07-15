package solution.bjjeire.selenium.web.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TimeoutSettings {
    private long pageLoadTimeout;
    private long scriptTimeout;
    private long elementWaitTimeout;
    private long waitForAjaxTimeout;
    private long waitUntilReadyTimeout;
    private long waitForJavaScriptAnimationsTimeout;
    private long waitForAngularTimeout;
    private long waitForPartialUrl;
    private long sleepInterval;
    private long validationsTimeout;
    private long elementToBeVisibleTimeout;
    private long elementToExistTimeout;
    private long elementToNotExistTimeout;
    private long elementToBeClickableTimeout;
    private long elementNotToBeVisibleTimeout;
    private long elementToHaveContentTimeout;
}
