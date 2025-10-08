package solutions.bjjeire.selenium.web.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import solutions.bjjeire.selenium.web.components.WebComponent;
import solutions.bjjeire.selenium.web.findstrategies.CssFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.DataTestIdFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.FindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.InnerTextContainingFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.TagFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.XPathFindStrategy;

@Service
public class ComponentCreateService extends WebService {

  private final ApplicationContext applicationContext;

  public ComponentCreateService(
      DriverService driverService, ApplicationContext applicationContext) {
    super(driverService);
    this.applicationContext = applicationContext;
  }

  public <TComponent extends WebComponent> TComponent byCss(
      Class<TComponent> componentClass, String css) {
    return by(componentClass, new CssFindStrategy(css));
  }

  public <TComponent extends WebComponent> TComponent byDataTestId(
      Class<TComponent> componentClass, String dataTestId) {
    return by(componentClass, new DataTestIdFindStrategy(dataTestId));
  }

  public <TComponent extends WebComponent> TComponent byXPath(
      Class<TComponent> componentClass, String xpath) {
    return by(componentClass, new XPathFindStrategy(xpath));
  }

  public <TComponent extends WebComponent> TComponent byTag(
      Class<TComponent> componentClass, String tag) {
    return by(componentClass, new TagFindStrategy(tag));
  }

  public <TComponent extends WebComponent> TComponent byInnerTextContaining(
      Class<TComponent> componentClass, String innerText) {
    return by(componentClass, new InnerTextContainingFindStrategy(innerText));
  }

  public <TComponent extends WebComponent> List<TComponent> allByCss(
      Class<TComponent> componentClass, String css) {
    return allBy(componentClass, new CssFindStrategy(css));
  }

  public <TComponent extends WebComponent> List<TComponent> allByXPath(
      Class<TComponent> componentClass, String xpath) {
    return allBy(componentClass, new XPathFindStrategy(xpath));
  }

  public <TComponent extends WebComponent> List<TComponent> allByTag(
      Class<TComponent> componentClass, String tag) {
    return allBy(componentClass, new TagFindStrategy(tag));
  }

  public <TComponent extends WebComponent> List<TComponent> allByDataTestId(
      Class<TComponent> componentClass, String dataTestId) {
    return allBy(componentClass, new DataTestIdFindStrategy(dataTestId));
  }

  public <TComponent extends WebComponent> List<TComponent> allByInnerTextContaining(
      Class<TComponent> componentClass, String innerText) {
    return allBy(componentClass, new InnerTextContainingFindStrategy(innerText));
  }

  private <TComponent extends WebComponent, TFindStrategy extends FindStrategy> TComponent by(
      Class<TComponent> componentClass, TFindStrategy findStrategy) {
    var component = applicationContext.getBean(componentClass);
    component.setFindStrategy(findStrategy);
    return component;
  }

  private <TComponent extends WebComponent, TFindStrategy extends FindStrategy>
      List<TComponent> allBy(Class<TComponent> componentClass, TFindStrategy findStrategy) {
    var nativeElements = getWrappedDriver().findElements(findStrategy.convert());
    List<TComponent> componentList = new ArrayList<>();
    for (int i = 0; i < nativeElements.size(); i++) {
      var component = applicationContext.getBean(componentClass);
      component.setFindStrategy(findStrategy);
      component.setElementIndex(i);
      componentList.add(component);
    }
    return componentList;
  }
}
