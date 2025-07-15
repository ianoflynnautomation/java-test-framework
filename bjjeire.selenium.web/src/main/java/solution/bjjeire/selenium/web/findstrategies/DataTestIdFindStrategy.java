package solution.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class DataTestIdFindStrategy extends FindStrategy{

    public DataTestIdFindStrategy(String value) {
        super(value);
    }

    @Override
    public By convert() {
        return By.cssSelector(String.format("[%s*='%s']","data-testid", getValue()));
    }

    @Override
    public String toString() {
        return String.format("%s containing %s", "data-testid", getValue());
    }
}
