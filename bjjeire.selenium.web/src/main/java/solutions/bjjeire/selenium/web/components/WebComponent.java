package solutions.bjjeire.selenium.web.components;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import solutions.bjjeire.selenium.web.components.contracts.BjjEireComponent;
import solutions.bjjeire.selenium.web.components.enums.AriaRole;
import solutions.bjjeire.selenium.web.components.enums.ScrollPosition;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.findstrategies.CssFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.DataTestIdFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.FindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.LabelTextFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.PlaceholderFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.RoleFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.TextFindStrategy;
import solutions.bjjeire.selenium.web.findstrategies.XPathFindStrategy;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentValidationService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategy;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope("prototype")
public abstract class WebComponent implements BjjEireComponent {

    private static final Logger log = LoggerFactory.getLogger(WebComponent.class);

    @Setter(AccessLevel.PROTECTED)
    private WebElement wrappedElement;
    @Getter
    @Setter
    protected WebComponent parentComponent;
    @Getter
    @Setter
    private SearchContext parentWrappedElement;
    @Getter
    @Setter
    private int elementIndex;
    @Getter
    @Setter
    private FindStrategy findStrategy;
    private final DriverService driverService;
    @Getter
    protected final JavaScriptService javaScriptService;
    @Getter
    protected final BrowserService browserService;
    @Getter
    protected final ComponentWaitService componentWaitService;
    private final List<WaitStrategy> waitStrategies;
    private final WebSettings webSettings;
    protected final ApplicationContext applicationContext;
    private final WaitStrategyFactory waitStrategyFactory;

    public WebComponent(
            DriverService driverService,
            JavaScriptService javaScriptService,
            BrowserService browserService,
            ComponentWaitService componentWaitService,
            WebSettings webSettings,
            ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory) {
        this.driverService = driverService;
        this.javaScriptService = javaScriptService;
        this.browserService = browserService;
        this.componentWaitService = componentWaitService;
        this.webSettings = webSettings;
        this.applicationContext = applicationContext;
        this.waitStrategyFactory = waitStrategyFactory;
        this.waitStrategies = new ArrayList<>();
    }

    public ComponentValidationService validator() {
        ComponentValidationService validator = applicationContext.getBean(ComponentValidationService.class);
        validator.setComponent(this);
        return validator;
    }

    public WebDriver getWrappedDriver() {
        return driverService.getWrappedDriver();
    }

    public <ComponentT extends WebComponent> ComponentT as(Class<ComponentT> componentClass) {
        if (this.getClass() == componentClass) {
            return (ComponentT) this;
        }

        var component = applicationContext.getBean(componentClass);
        component.setParentComponent(this.parentComponent);
        component.setParentWrappedElement(this.parentWrappedElement);
        component.setFindStrategy(this.findStrategy);
        component.setElementIndex(this.elementIndex);

        return component;
    }

    public WebElement getWrappedElement() {
        try {
            if (wrappedElement != null) {
                wrappedElement.isDisplayed();
                return wrappedElement;
            } else {
                return findElement();
            }
        } catch (ElementNotInteractableException ex) {
            scrollToVisible();
            return findElement();
        } catch (StaleElementReferenceException ex) {
            return findElement();
        } catch (WebDriverException ex) {
            toExist().waitToBe();
            return wrappedElement;
        }
    }

    public <TElementType extends WebComponent> TElementType toExist() {
        ensureState(waitStrategyFactory.exist());
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeVisible() {
        ensureState(waitStrategyFactory.beVisible());
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable() {
        ensureState(waitStrategyFactory.beClickable());
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable(long timeoutInterval, long sleepInterval) {
        ensureState(waitStrategyFactory.beClickable(timeoutInterval, sleepInterval));
        return (TElementType) this;
    }

    public <TElementType extends WebComponent, TWaitStrategy extends WaitStrategy> TElementType to(
            Class<TWaitStrategy> waitClass, TElementType element) {
        TWaitStrategy waitStrategy = applicationContext.getBean(waitClass);
        element.ensureState(waitStrategy);
        return element;
    }

    protected <TComponent extends WebComponent, TFindStrategy extends FindStrategy> TComponent create(
            Class<TComponent> componentClass, TFindStrategy findStrategy) {
        findElement();
        TComponent component = applicationContext.getBean(componentClass);
        component.setFindStrategy(findStrategy);
        component.setParentComponent(this);
        component.setParentWrappedElement(this.getWrappedElement());
        return component;
    }

    protected <TComponent extends WebComponent, TFindStrategy extends FindStrategy> List<TComponent> createAll(
            Class<TComponent> componentClass, TFindStrategy findStrategy) {
        findElement();
        List<TComponent> componentList = new ArrayList<>();
        var nativeElements = wrappedElement.findElements(findStrategy.convert());
        for (int i = 0; i < nativeElements.size(); i++) {
            var component = applicationContext.getBean(componentClass);
            component.setFindStrategy(findStrategy);
            component.setElementIndex(i);
            component.setParentWrappedElement(wrappedElement);
            componentList.add(component);
        }
        return componentList;
    }

    public WebElement findElement() {
        if (waitStrategies.isEmpty()) {
            waitStrategies.add(waitStrategyFactory.exist());
        }

        try {
            for (var waitStrategy : waitStrategies) {
                componentWaitService.wait(this, waitStrategy);
            }
            wrappedElement = findNativeElement();
            scrollToMakeElementVisible(wrappedElement);
            waitStrategies.clear();
        } catch (Exception ex) {
            var formattedException = String.format("The component: \n" +
                    "     Type: %s" +
                    "  Locator: %s" +
                    "  URL: %s\"%n" +
                    "Was not found on the page or didn't fulfill the specified conditions.%n%n",
                    getClass().getSimpleName(), findStrategy.toString(), getWrappedDriver().getCurrentUrl());
            throw new NotFoundException(formattedException, ex);
        }
        return wrappedElement;
    }

    public String getComponentName() {
        return String.format("%s (%s)", getComponentClass().getSimpleName(), findStrategy.toString());
    }

    public Class<?> getComponentClass() {
        return getClass();
    }

    public void waitToBe() {
        findElement();
    }

    public void scrollToVisible() {
        scrollToVisible(getWrappedElement(), false, ScrollPosition.CENTER);
    }

    public String getAttribute(String name) {
        return getWrappedElement().getAttribute(name);
    }

    public void ensureState(WaitStrategy waitStrategy) {
        waitStrategies.add(waitStrategy);
    }

    public void hover() {
        Actions actions = new Actions(getWrappedDriver());
        actions.moveToElement(getWrappedElement()).build().perform();
    }

    public String getTitle() {
        return getAttribute("title");
    }

    public <TComponent extends WebComponent> TComponent createByRole(Class<TComponent> componentClass, AriaRole role,
            String... name) {
        return create(componentClass, new RoleFindStrategy(role, name));
    }

    public <TComponent extends WebComponent> TComponent createByLabelText(Class<TComponent> componentClass,
            String labelText) {
        return create(componentClass, new LabelTextFindStrategy(labelText));
    }

    public <TComponent extends WebComponent> TComponent createByPlaceholderText(Class<TComponent> componentClass,
            String placeholderText) {
        return create(componentClass, new PlaceholderFindStrategy(placeholderText));
    }

    public <TComponent extends WebComponent> TComponent createByText(Class<TComponent> componentClass, String text) {
        return create(componentClass, new TextFindStrategy(text));
    }

    public <TComponent extends WebComponent> TComponent createByDataTestId(Class<TComponent> componentClass,
            String dataTestId) {
        return create(componentClass, new DataTestIdFindStrategy(dataTestId));
    }

    public <TComponent extends WebComponent> List<TComponent> createAllByDataTestId(Class<TComponent> componentClass,
            String dataTestId) {
        return createAll(componentClass, new DataTestIdFindStrategy(dataTestId));
    }

    public <TComponent extends WebComponent> TComponent createByCss(Class<TComponent> componentClass, String css) {
        return create(componentClass, new CssFindStrategy(css));
    }

    public <TComponent extends WebComponent> TComponent createByXPath(Class<TComponent> componentClass, String xpath) {
        return create(componentClass, new XPathFindStrategy(xpath));
    }

    public <TComponent extends WebComponent> List<TComponent> createAllByXPath(Class<TComponent> componentClass,
            String xpath) {
        return createAll(componentClass, new XPathFindStrategy(xpath));
    }

    private WebElement findNativeElement() {
        SearchContext context = (parentWrappedElement != null) ? parentWrappedElement : getWrappedDriver();
        return context.findElements(findStrategy.convert()).get(elementIndex);
    }

    private void scrollToMakeElementVisible(WebElement wrappedElement) {
        if (webSettings.getAutomaticallyScrollToVisible()) {
            scrollToVisible(wrappedElement, false, ScrollPosition.CENTER);
        }
    }

    private void scrollToVisible(WebElement element, boolean shouldWait, ScrollPosition scrollPosition) {
        try {
            javaScriptService.execute("arguments[0].scrollIntoView({ block: \"" + scrollPosition.getValue()
                    + "\", behavior: \"instant\", inline: \"nearest\" });", element);
            if (shouldWait) {
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            log.warn("JavaScript scrollIntoView failed. This may be non-critical.", ex);
        }
    }

    public boolean isVisible() {
        try {
            return getWrappedElement().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickInternal() {
        long toBeClickableTimeout = webSettings.getTimeoutSettings().getElementToBeClickableTimeout();
        long sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();

        FluentWait<WebDriver> wait = new FluentWait<>(getWrappedDriver())
                .withTimeout(Duration.ofSeconds(toBeClickableTimeout))
                .pollingEvery(Duration.ofSeconds(sleepInterval > 0 ? sleepInterval : 1));

        try {
            wait.until(x -> tryClick());
        } catch (TimeoutException e) {
            log.warn("Default click timed out after {} seconds. Attempting JavaScript click as a fallback.",
                    toBeClickableTimeout);
            javaScriptService.execute("arguments[0].click()", findElement());
        }
    }

    private boolean tryClick() {
        try {
            toBeVisible().toBeClickable().findElement().click();
            return true;
        } catch (ElementNotInteractableException e) {
            log.warn("ElementNotInteractableException encountered. Retrying after scroll.", e);
            scrollToVisible();
            return false;
        } catch (StaleElementReferenceException e) {
            log.warn("StaleElementReferenceException encountered. Retrying with a new find.", e);
            findElement();
            return false;
        } catch (WebDriverException e) {
            log.warn("WebDriverException encountered during click attempt. Retrying.", e);
            return false;
        }
    }

    protected void defaultClick() {
        clickInternal();
    }

    protected void defaultCheck() {
        this.toExist().toBeClickable().waitToBe();
        if (!getWrappedElement().isSelected()) {
            clickInternal();
        }
    }

    protected void defaultUncheck() {
        toExist().toBeClickable().waitToBe();
        if (getWrappedElement().isSelected()) {
            clickInternal();
        }
    }

    protected String defaultGetText() {
        try {
            return Optional.ofNullable(getWrappedElement().getText()).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getText()).orElse("");
        }
    }

    protected boolean defaultGetDisabledAttribute() {
        try {
            var valueAttr = Optional.ofNullable(getAttribute("disabled")).orElse("false");
            return valueAttr.equalsIgnoreCase("true");
        } catch (StaleElementReferenceException e) {
            var valueAttr = Optional.ofNullable(findElement().getAttribute("disabled")).orElse("false");
            return valueAttr.equalsIgnoreCase("true");
        }
    }

    protected String defaultGetInnerHtmlAttribute() {
        try {
            return Optional.ofNullable(getAttribute("innerHTML")).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getAttribute("innerHTML")).orElse("");
        }
    }

    @SneakyThrows
    protected String defaultGetHref() {
        try {
            return unescapeHtml4(URLDecoder.decode(Optional.ofNullable(getAttribute("href")).orElse(""),
                    StandardCharsets.UTF_8.name()));
        } catch (StaleElementReferenceException e) {
            return unescapeHtml4(URLDecoder.decode(Optional.ofNullable(findElement().getAttribute("href")).orElse(""),
                    StandardCharsets.UTF_8.name()));
        }
    }

    protected String defaultGetAriaLabel() {
        try {
            return Optional.ofNullable(getAttribute("aria-label")).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getAttribute("aria-label")).orElse("");
        }
    }

    protected String defaultGetRelAttribute() {
        try {
            return Optional.ofNullable(getAttribute("rel")).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getAttribute("rel")).orElse("");
        }
    }

    protected String defaultGetTargetAttribute() {
        try {
            return Optional.ofNullable(getAttribute("target")).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getAttribute("target")).orElse("");
        }
    }

    protected String defaultGetValue() {
        try {
            return Optional.ofNullable(getAttribute("value")).orElse("");
        } catch (StaleElementReferenceException e) {
            return Optional.ofNullable(findElement().getAttribute("value")).orElse("");
        }
    }

    protected void defaultSelectByText(String value) {
        new Select(getWrappedElement()).selectByVisibleText(value);
    }

    protected void defaultSetText(String value) {
        getWrappedElement().clear();
        getWrappedElement().sendKeys(value);
    }

    protected void defaultSelectByIndex(int value) {
        new Select(getWrappedElement()).selectByIndex(value);
    }
}
