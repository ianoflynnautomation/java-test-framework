package solutions.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class PlaceholderFindStrategy extends FindStrategy {
    private final String placeholderText;

    public PlaceholderFindStrategy(String placeholderText) {
        super(buildXPath(placeholderText));
        this.placeholderText = placeholderText;
    }

    private static String buildXPath(String placeholderText) {
        return String.format(".//*[@placeholder='%s']", placeholderText);
    }

    @Override
    public By convert() {
        return By.xpath(getValue());
    }

    @Override
    public String toString() {
        return String.format("by placeholder = '%s'", placeholderText);
    }
}