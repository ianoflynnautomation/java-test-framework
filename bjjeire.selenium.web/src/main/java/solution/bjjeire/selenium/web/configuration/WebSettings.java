package solution.bjjeire.selenium.web.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebSettings {
    private String baseUrl;
    private String executionType;
    private String defaultLifeCycle;
    private String defaultBrowser;
    private Integer defaultBrowserWidth = 0;
    private Integer defaultBrowserHeight = 0;
    private List<GridSettings> gridSettings;

    private int artificialDelayBeforeAction;
    private TimeoutSettings timeoutSettings;

    private Boolean automaticallyScrollToVisible;
    private Boolean waitUntilReadyOnElementFound;

    private Boolean screenshotsOnFailEnabled;
    private String screenshotsSaveLocation;

    private Boolean videosOnFailEnabled;
    private String videosSaveLocation;
}