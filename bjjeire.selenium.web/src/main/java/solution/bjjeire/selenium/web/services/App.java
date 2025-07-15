package solution.bjjeire.selenium.web.services;

import solution.bjjeire.selenium.web.infrastructure.DriverService;
import solution.bjjeire.selenium.web.pages.WebPage;
import solutions.bjjeire.core.infrastructure.BjjEireApp;
import solutions.bjjeire.core.infrastructure.PageObjectModel;
import solutions.bjjeire.core.utilities.SingletonFactory;

public class App implements BjjEireApp{

    private boolean disposed = false;

    public NavigationService navigate() {
        return SingletonFactory.getInstance(NavigationService.class);
    }

    public BrowserService browser() {
        return SingletonFactory.getInstance(BrowserService.class);
    }

    public CookiesService cookies() {
        return SingletonFactory.getInstance(CookiesService.class);
    }

    public JavaScriptService script() {
        return SingletonFactory.getInstance(JavaScriptService.class);
    }

    public ComponentCreateService create() {
        return SingletonFactory.getInstance(ComponentCreateService.class);
    }

    public ComponentWaitService waitFor() {
        return SingletonFactory.getInstance(ComponentWaitService.class);
    }

    @Override
    public void addDriverOptions(String key, String value) {
        DriverService.addDriverOptions(key, value);
    }

    public <TPage extends WebPage> TPage goTo(Class<TPage> pageOf, Object... args) {
        var page = SingletonFactory.getInstance(pageOf, args);
        assert page != null;
        page.open();

        return page;
    }

    @Override
    public void close() {
        if (disposed) {
            return;
        }

        DriverService.close();
        SingletonFactory.clear();
        disposed = true;
    }
}
