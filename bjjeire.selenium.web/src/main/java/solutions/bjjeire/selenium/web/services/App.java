package solutions.bjjeire.selenium.web.services;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.infrastructure.BjjEireApp;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.infrastructure.PageObjectModel;
import solutions.bjjeire.selenium.web.pages.WebPage;

/**
 * The primary application facade, managed by Spring as a singleton component.
 * It provides a central, injectable point of access to the framework's services.
 */
@Component
public class App implements BjjEireApp {

    private final NavigationService navigationService;
    private final BrowserService browserService;
    private final CookiesService cookiesService;
    private final JavaScriptService javaScriptService;
    private final ComponentCreateService componentCreateService;
    private final ComponentWaitService componentWaitService;
    private final DriverService driverService;
    private final ApplicationContext applicationContext;

    @Autowired
    public App(NavigationService navigationService,
               BrowserService browserService,
               CookiesService cookiesService,
               JavaScriptService javaScriptService,
               ComponentCreateService componentCreateService,
               ComponentWaitService componentWaitService,
               DriverService driverService,
               ApplicationContext applicationContext) {
        this.navigationService = navigationService;
        this.browserService = browserService;
        this.cookiesService = cookiesService;
        this.javaScriptService = javaScriptService;
        this.componentCreateService = componentCreateService;
        this.componentWaitService = componentWaitService;
        this.driverService = driverService;
        this.applicationContext = applicationContext;
    }

    @Override
    public NavigationService navigate() {
        return navigationService;
    }

    @Override
    public BrowserService browser() {
        return browserService;
    }

    @Override
    public CookiesService cookies() {
        return cookiesService;
    }

    @Override
    public JavaScriptService script() {
        return javaScriptService;
    }

    @Override
    public ComponentCreateService create() {
        return componentCreateService;
    }

    @Override
    public ComponentWaitService waitFor() {
        return componentWaitService;
    }


    public <TPage extends WebPage> TPage goTo(Class<TPage> pageClass) {
        TPage page = applicationContext.getBean(pageClass);
        page.open();
        return page;
    }

    @Override
    public <TPage extends PageObjectModel> TPage createPage(Class<TPage> pageClass, Object... args) {
        return applicationContext.getBean(pageClass, args);
    }

    @Override
    public <TSection extends PageObjectModel> TSection createSection(Class<TSection> sectionClass, Object... args) {
        return applicationContext.getBean(sectionClass, args);
    }

    @Override
    @PreDestroy
    public void close() {
        driverService.close();
    }
}
