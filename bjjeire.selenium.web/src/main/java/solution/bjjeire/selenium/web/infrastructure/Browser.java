package solution.bjjeire.selenium.web.infrastructure;

import java.util.Arrays;

public enum Browser {
    CHROME("chrome"),
    CHROME_HEADLESS("chrome_headless"),
    CHROME_MOBILE("chrome_mobile"),
    FIREFOX("firefox"),
    FIREFOX_HEADLESS("firefox_headless"),
    EDGE("edge"),
    EDGE_HEADLESS("edge_headless"),
    OPERA("opera"),
    SAFARI("safari"),
    INTERNET_EXPLORER("ie"),
    NOT_SET("not_set");

    private final String value;

    Browser(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Browser fromText(String text) {
        return Arrays.stream(values())
                .filter(l -> l.value.equalsIgnoreCase(text))
                .findFirst().orElse(Browser.CHROME);
    }
}
