package solutions.bjjeire.selenium.web.components;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.contracts.ComponentAriaLabel;
import solutions.bjjeire.selenium.web.components.contracts.ComponentHref;
import solutions.bjjeire.selenium.web.components.contracts.ComponentRel;
import solutions.bjjeire.selenium.web.components.contracts.ComponentTarget;
import solutions.bjjeire.selenium.web.components.contracts.ComponentText;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Anchor extends WebComponent
    implements ComponentText, ComponentHref, ComponentTarget, ComponentRel, ComponentAriaLabel {

  public Anchor(
      DriverService driverService,
      JavaScriptService javaScriptService,
      BrowserService browserService,
      ComponentWaitService componentWaitService,
      WebSettings webSettings,
      ApplicationContext applicationContext,
      WaitStrategyFactory waitStrategyFactory) {
    super(
        driverService,
        javaScriptService,
        browserService,
        componentWaitService,
        webSettings,
        applicationContext,
        waitStrategyFactory);
  }

  public void click() {
    defaultClick();
  }

  @Override
  public String getHref() {
    return defaultGetHref();
  }

  @Override
  public String getText() {
    return defaultGetText();
  }

  public String getHtml() {
    return defaultGetInnerHtmlAttribute();
  }

  @Override
  public String getTarget() {
    return defaultGetTargetAttribute();
  }

  @Override
  public String getRel() {
    return defaultGetRelAttribute();
  }

  @Override
  public String getAriaLabel() {
    return defaultGetAriaLabel();
  }
}
