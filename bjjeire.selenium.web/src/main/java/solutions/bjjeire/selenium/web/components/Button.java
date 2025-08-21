package solutions.bjjeire.selenium.web.components;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.selenium.web.components.contracts.ComponentDisabled;
import solutions.bjjeire.selenium.web.components.contracts.ComponentText;
import solutions.bjjeire.selenium.web.components.contracts.ComponentValue;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Button extends WebComponent implements ComponentValue, ComponentText, ComponentDisabled {

    public Button(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
            ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory);
    }

    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

    public void click() {
        defaultClick();
    }

    @Override
    public String getText() {
        return defaultGetText();
    }

    @Override
    public String getValue() {
        return defaultGetValue();
    }

    @Override
    public boolean isDisabled() {
        return defaultGetDisabledAttribute();
    }
}
