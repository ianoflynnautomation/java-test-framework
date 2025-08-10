package solutions.bjjeire.selenium.web.components.contracts;

import org.openqa.selenium.WebElement;

import solutions.bjjeire.selenium.web.services.ComponentValidationService;

public interface BjjEireComponent {
    Class<?> getComponentClass();

    WebElement getWrappedElement();

    //FindStrategy getFindStrategy();

    String getAttribute(String attributeName);

    ComponentValidationService validator();
}
