package solution.bjjeire.selenium.web.pages;


import solution.bjjeire.selenium.web.services.*;
import solution.bjjeire.selenium.web.services.*;
import solutions.bjjeire.core.infrastructure.PageObjectModel;

public abstract class WebPage implements PageObjectModel {

    public BrowserService browser() {
        return app().browser();
    }

    public ComponentCreateService create() {
        return app().create();
    }

    public JavaScriptService javaScript() {
        return app().script();
    }

    public App app() {
        return new App();
    }

    public NavigationService navigate() {
        return new NavigationService();
    }

    protected String getUrl() {
        return "";
    }

    public void open() {
        navigate().to(getUrl());
//        waitForPageLoad();
    }

//    protected void waitForPageLoad() {
//        browser().waitForAjax();
//    }
}
