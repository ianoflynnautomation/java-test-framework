package solutions.bjjeire.selenium.web.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.contracts.*;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope("prototype")
public class Anchor extends WebComponent
        implements ComponentText, ComponentHref, ComponentTarget, ComponentRel, ComponentAriaLabel {

    @Autowired
    public Anchor(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
            ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory);
    }

    public void click() {
        defaultClick();
    }

    public String getHref() {
        return defaultGetHref();
    }

    public String getText() {
        return defaultGetText();
    }

    public String getHtml() {
        return defaultGetInnerHtmlAttribute();
    }

    public String getTarget() {
        return defaultGetTargetAttribute();
    }

    public String getRel() {
        return defaultGetRelAttribute();
    }

    public String getAriaLabel() {
        return defaultGetAriaLabel();
    }
}
