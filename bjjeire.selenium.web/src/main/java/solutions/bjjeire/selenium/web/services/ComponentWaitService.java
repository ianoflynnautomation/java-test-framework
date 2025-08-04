package solutions.bjjeire.selenium.web.services;

import org.springframework.stereotype.Service;

import solutions.bjjeire.selenium.web.components.WebComponent;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategy;

@Service
public class ComponentWaitService extends WebService {

    public ComponentWaitService(DriverService driverService) {
        super(driverService);
    }

    public void wait(WebComponent component, WaitStrategy waitStrategy) {
        if (component.getParentWrappedElement() == null) {
            waitStrategy.waitUntil(driverService, getWrappedDriver(), component.getFindStrategy().convert());
        } else {
            waitStrategy.waitUntil(driverService, component.getParentWrappedElement(),
                    component.getFindStrategy().convert());
        }
    }
}