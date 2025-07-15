package solution.bjjeire.selenium.web.services;

import solution.bjjeire.selenium.web.components.WebComponent;
import solution.bjjeire.selenium.web.infrastructure.DriverService;
import solution.bjjeire.selenium.web.waitstrategies.WaitStrategy;

public class ComponentWaitService extends WebService {
    public void wait(WebComponent component, WaitStrategy waitStrategy) {
        if (component.getParentWrappedElement() == null) {
            waitStrategy.waitUntil(DriverService.getWrappedDriver(), component.getFindStrategy().convert());
        } else {
            waitStrategy.waitUntil(component.getParentWrappedElement(), component.getFindStrategy().convert());
        }
    }
}