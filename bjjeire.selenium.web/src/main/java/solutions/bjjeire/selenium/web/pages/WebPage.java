package solutions.bjjeire.selenium.web.pages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

/**
 * Abstract base class for all Page Objects.
 * It uses constructor injection to receive all dependencies, ensuring they are
 * available to all subclasses in a consistent manner.
 */
public abstract class WebPage {

    protected final DriverService driverService;
    protected final JavaScriptService javaScriptService;
    protected final BrowserService browserService;
    protected final ComponentWaitService componentWaitService;
    protected final WebSettings webSettings;
    protected final ApplicationContext applicationContext;
    protected final WaitStrategyFactory waitStrategyFactory;
    protected final NavigationService navigationService;
    protected final ComponentCreateService componentCreateService;

    @Autowired
    public WebPage(DriverService driverService,
                   JavaScriptService javaScriptService,
                   BrowserService browserService,
                   ComponentWaitService componentWaitService,
                   WebSettings webSettings,
                   ApplicationContext applicationContext,
                   WaitStrategyFactory waitStrategyFactory,
                   NavigationService navigationService,
                   ComponentCreateService componentCreateService) {
        this.driverService = driverService;
        this.javaScriptService = javaScriptService;
        this.browserService = browserService;
        this.componentWaitService = componentWaitService;
        this.webSettings = webSettings;
        this.applicationContext = applicationContext;
        this.waitStrategyFactory = waitStrategyFactory;
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