package solutions.bjjeire.selenium.web.components;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.contracts.ComponentText;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope("prototype")
public class Label extends WebComponent implements ComponentText {
    @Autowired
    public Label(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService, ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext, WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext, waitStrategyFactory);
    }

    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

//    @Override
    public String getText() {
        return defaultGetText();
    }
}
