package solutions.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class InnerTextContainingFindStrategy extends FindStrategy {

    public InnerTextContainingFindStrategy(String value) {
        super(value);
    }

    @Override
    public By convert() {
        return By.xpath(String.format("//*[contains(text(), '%s')]", getValue()));
    }

    @Override
    public String toString() {
        return String.format("text containing %s", getValue());
    }
}
