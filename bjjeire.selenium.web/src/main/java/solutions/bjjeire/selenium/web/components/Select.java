package solutions.bjjeire.selenium.web.components;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.contracts.ComponentDisabled;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;
import solutions.bjjeire.core.utilities.InstanceFactory;

@Component
@Scope("prototype")
public class Select extends WebComponent implements ComponentDisabled {

    @Autowired
    public Select(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService, ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext, WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext, waitStrategyFactory);
    }


    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

    public Option getSelected() {
        org.openqa.selenium.support.ui.Select nativeSelect = new org.openqa.selenium.support.ui.Select(findElement());
        var optionComponent = InstanceFactory.create(Option.class);
        optionComponent.setFindStrategy(getFindStrategy());
        optionComponent.setElementIndex(0);
        optionComponent.setWrappedElement(nativeSelect.getFirstSelectedOption());
        return optionComponent;
    }

    public void selectByText(String text) {
        defaultSelectByText(text);
    }

    public void selectByIndex(int index) {
        defaultSelectByIndex(index);
    }

    @Override
    public boolean isDisabled() { return defaultGetDisabledAttribute(); }
}
