package solutions.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class LabelTextFindStrategy extends FindStrategy {

    private final String labelText;

    public LabelTextFindStrategy(String labelText) {
        super(buildXPath(labelText));
        this.labelText = labelText;
    }

    private static String buildXPath(String labelText) {
        // This XPath finds a label by its text, gets its 'for' attribute,
        // and then finds the input element with the matching 'id'.
        return String.format(
                ".//*[self::input or self::textarea or self::select][@id=(//label[normalize-space(.)='%s']/@for)]",
                labelText);
    }

    @Override
    public By convert() {
        return By.xpath(getValue());
    }

    @Override
    public String toString() {
        return String.format("by label text = '%s'", labelText);
    }
}