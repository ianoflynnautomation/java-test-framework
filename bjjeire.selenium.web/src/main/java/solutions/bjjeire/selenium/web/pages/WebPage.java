package solutions.bjjeire.selenium.web.pages;

import lombok.RequiredArgsConstructor;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.NavigationService;

@RequiredArgsConstructor
public abstract class WebPage {

  protected final NavigationService navigationService;
  protected final ComponentCreateService componentCreateService;

  protected abstract String getUrl();

  public void open() {
    navigationService.to(getUrl());
  }

  protected ComponentCreateService create() {
    return componentCreateService;
  }
}
