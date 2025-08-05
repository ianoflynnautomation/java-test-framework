package solutions.bjjeire.selenium.web.pages;

import org.springframework.context.ApplicationContext;

import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

/**
 * Abstract base class for all Page Objects.
 * It uses constructor injection to receive all dependencies, ensuring they are
 * available to all subclasses in a consistent manner.
 */
public abstract class WebPage {

    protected final NavigationService navigationService;
    protected final ComponentCreateService componentCreateService;

    public WebPage(NavigationService navigationService,
            ComponentCreateService componentCreateService) {

        this.navigationService = navigationService;
        this.componentCreateService = componentCreateService;
    }

    protected abstract String getUrl();

    public void open() {
        navigationService.to(getUrl());
    }

    protected ComponentCreateService create() {
        return componentCreateService;
    }
}