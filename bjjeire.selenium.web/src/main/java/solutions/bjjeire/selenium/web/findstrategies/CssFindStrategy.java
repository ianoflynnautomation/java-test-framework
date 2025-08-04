package solutions.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class CssFindStrategy extends FindStrategy {

    public CssFindStrategy(String value) {
        super(value);
    }

    @Override
    public By convert() {
        return By.cssSelector(getValue());
    }

    @Override
    public String toString() {
        return String.format("css = %s", getValue());
    }
}
