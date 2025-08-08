package solutions.bjjeire.selenium.web.pages;

import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.NavigationService;

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