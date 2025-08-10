package solutions.bjjeire.selenium.web.components;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;

import lombok.extern.slf4j.Slf4j;
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
import net.logstash.logback.argument.StructuredArguments; // Add this import
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

@Slf4j
@Component
@Scope("prototype")
public abstract class WebComponent implements BjjEireComponent {

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
        log.info("Casting component",
                StructuredArguments.keyValue("from", this.getClass().getSimpleName()),
                StructuredArguments.keyValue("to", componentClass.getSimpleName()));
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
            log.warn("Element not interactable, scrolling to make it visible",
                    StructuredArguments.keyValue("component", getComponentName()),
                    ex);
            scrollToVisible();
            return findElement();
        } catch (StaleElementReferenceException ex) {
            log.warn("Stale element reference, attempting to find again",
                    StructuredArguments.keyValue("component", getComponentName()),
                    ex);
            return findElement();
        } catch (WebDriverException ex) {
            log.warn("WebDriverException during getWrappedElement, waiting for element to exist",
                    StructuredArguments.keyValue("component", getComponentName()),
                    ex);
            toExist().waitToBe();
            return wrappedElement;
        }
    }

    public <TElementType extends WebComponent> TElementType toExist() {
        ensureState(waitStrategyFactory.exist());
        log.debug("Adding 'toExist' wait strategy",
                StructuredArguments.keyValue("component", getComponentName()));
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeVisible() {
        ensureState(waitStrategyFactory.beVisible());
        log.debug("Adding 'toBeVisible' wait strategy",
                StructuredArguments.keyValue("component", getComponentName()));
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable() {
        ensureState(waitStrategyFactory.beClickable());
        log.debug("Adding 'toBeClickable' wait strategy",
                StructuredArguments.keyValue("component", getComponentName()));
        return (TElementType) this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable(long timeoutInterval, long sleepInterval) {
        ensureState(waitStrategyFactory.beClickable(timeoutInterval, sleepInterval));
        log.debug("Adding 'toBeClickable' wait strategy with custom timeout",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("timeoutInterval", timeoutInterval),
                StructuredArguments.keyValue("sleepInterval", sleepInterval));
        return (TElementType) this;
    }

    public <TElementType extends WebComponent, TWaitStrategy extends WaitStrategy> TElementType to(
            Class<TWaitStrategy> waitClass, TElementType element) {
        TWaitStrategy waitStrategy = applicationContext.getBean(waitClass);
        element.ensureState(waitStrategy);
        log.debug("Adding custom wait strategy to element",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("waitStrategyClass", waitClass.getSimpleName()));
        return element;
    }

    protected <TComponent extends WebComponent, TFindStrategy extends FindStrategy> TComponent create(
            Class<TComponent> componentClass, TFindStrategy findStrategy) {
        findElement();
        TComponent component = applicationContext.getBean(componentClass);
        component.setFindStrategy(findStrategy);
        component.setParentComponent(this);
        component.setParentWrappedElement(this.getWrappedElement());
        log.info("Creating child component",
                StructuredArguments.keyValue("parentComponent", getComponentName()),
                StructuredArguments.keyValue("childComponent", component.getComponentName()));
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
        log.info("Creating a list of child components",
                StructuredArguments.keyValue("parentComponent", getComponentName()),
                StructuredArguments.keyValue("childComponentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("count", nativeElements.size()));
        return componentList;
    }

    public WebElement findElement() {
        log.info("Attempting to find element",
                StructuredArguments.keyValue("component", getComponentName()));

        if (waitStrategies.isEmpty()) {
            waitStrategies.add(waitStrategyFactory.exist());
            log.debug("No wait strategies specified, defaulting to 'exist' strategy",
                    StructuredArguments.keyValue("component", getComponentName()));
        }

        try {
            for (var waitStrategy : waitStrategies) {
                componentWaitService.wait(this, waitStrategy);
            }
            wrappedElement = findNativeElement();
            scrollToMakeElementVisible(wrappedElement);
            waitStrategies.clear();
            log.info("Element found successfully",
                    StructuredArguments.keyValue("component", getComponentName()));
        } catch (Exception ex) {
            var formattedException = """
                   The component:
                   Type: %s
                   Locator: %s
                   URL: %s
                   Was not found on the page or didn't fulfill the specified conditions
                   """.formatted(
                    getClass().getSimpleName(),
                    findStrategy.toString(),
                    getWrappedDriver().getCurrentUrl());

            log.error(formattedException,
                    StructuredArguments.keyValue("componentType", getClass().getSimpleName()),
                    StructuredArguments.keyValue("locator", findStrategy.toString()),
                    StructuredArguments.keyValue("currentUrl", getWrappedDriver().getCurrentUrl()),
                    ex);
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
        log.info("Waiting for component to be in specified state",
                StructuredArguments.keyValue("component", getComponentName()));
    }

    public void scrollToVisible() {
        log.debug("Scrolling to make element visible",
                StructuredArguments.keyValue("component", getComponentName()));
        scrollToVisible(getWrappedElement(), false, ScrollPosition.CENTER);
    }

    public String getAttribute(String name) {
        String value = getWrappedElement().getAttribute(name);
        log.debug("Getting attribute for element",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("attributeName", name),
                StructuredArguments.keyValue("attributeValue", value));
        return value;
    }

    public void ensureState(WaitStrategy waitStrategy) {
        waitStrategies.add(waitStrategy);
    }

    public void hover() {
        Actions actions = new Actions(getWrappedDriver());
        actions.moveToElement(getWrappedElement()).build().perform();
        log.info("Hovering over element",
                StructuredArguments.keyValue("component", getComponentName()));
    }

    public String getTitle() {
        return getAttribute("title");
    }

    public <TComponent extends WebComponent> TComponent createByRole(Class<TComponent> componentClass, AriaRole role,
                                                                     String... name) {
        log.info("Creating component by role",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("ariaRole", role.name()));
        return create(componentClass, new RoleFindStrategy(role, name));
    }

    public <TComponent extends WebComponent> TComponent createByLabelText(Class<TComponent> componentClass,
                                                                          String labelText) {
        log.info("Creating component by label text",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("labelText", labelText));
        return create(componentClass, new LabelTextFindStrategy(labelText));
    }

    public <TComponent extends WebComponent> TComponent createByPlaceholderText(Class<TComponent> componentClass,
                                                                                String placeholderText) {
        log.info("Creating component by placeholder text",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("placeholderText", placeholderText));
        return create(componentClass, new PlaceholderFindStrategy(placeholderText));
    }

    public <TComponent extends WebComponent> TComponent createByText(Class<TComponent> componentClass, String text) {
        log.info("Creating component by text",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("text", text));
        return create(componentClass, new TextFindStrategy(text));
    }

    public <TComponent extends WebComponent> TComponent createByDataTestId(Class<TComponent> componentClass,
                                                                           String dataTestId) {
        log.info("Creating component by data-test-id",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("dataTestId", dataTestId));
        return create(componentClass, new DataTestIdFindStrategy(dataTestId));
    }

    public <TComponent extends WebComponent> List<TComponent> createAllByDataTestId(Class<TComponent> componentClass,
                                                                                    String dataTestId) {
        log.info("Creating all components by data-test-id",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("dataTestId", dataTestId));
        return createAll(componentClass, new DataTestIdFindStrategy(dataTestId));
    }

    public <TComponent extends WebComponent> TComponent createByCss(Class<TComponent> componentClass, String css) {
        log.info("Creating component by CSS selector",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("cssSelector", css));
        return create(componentClass, new CssFindStrategy(css));
    }

    public <TComponent extends WebComponent> TComponent createByXPath(Class<TComponent> componentClass, String xpath) {
        log.info("Creating component by XPath",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("xpath", xpath));
        return create(componentClass, new XPathFindStrategy(xpath));
    }

    public <TComponent extends WebComponent> List<TComponent> createAllByXPath(Class<TComponent> componentClass,
                                                                               String xpath) {
        log.info("Creating all components by XPath",
                StructuredArguments.keyValue("componentClass", componentClass.getSimpleName()),
                StructuredArguments.keyValue("xpath", xpath));
        return createAll(componentClass, new XPathFindStrategy(xpath));
    }

    private WebElement findNativeElement() {
        SearchContext context = (parentWrappedElement != null) ? parentWrappedElement : getWrappedDriver();
        return context.findElements(findStrategy.convert()).get(elementIndex);
    }

    private void scrollToMakeElementVisible(WebElement wrappedElement) {
        if (webSettings.isAutomaticallyScrollToVisible()) {
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
            log.warn("JavaScript scrollIntoView failed. This may be non-critical.",
                    StructuredArguments.keyValue("component", getComponentName()),
                    ex);
        }
    }

    public boolean isVisible() {
        try {
            boolean isDisplayed = getWrappedElement().isDisplayed();
            log.debug("Checking visibility of element",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("isVisible", isDisplayed));
            return isDisplayed;
        } catch (Exception e) {
            log.debug("Element is not visible",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("isVisible", false));
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
                    StructuredArguments.keyValue("timeoutSeconds", toBeClickableTimeout),
                    StructuredArguments.keyValue("component", getComponentName()),
                    e);
            javaScriptService.execute("arguments[0].click()", findElement());
        }
    }

    private boolean tryClick() {
        try {
            toBeVisible().toBeClickable().findElement().click();
            log.info("Successfully clicked element",
                    StructuredArguments.keyValue("component", getComponentName()));
            return true;
        } catch (ElementNotInteractableException e) {
            log.warn("ElementNotInteractableException encountered. Retrying after scroll.",
                    StructuredArguments.keyValue("component", getComponentName()),
                    e);
            scrollToVisible();
            return false;
        } catch (StaleElementReferenceException e) {
            log.warn("StaleElementReferenceException encountered. Retrying with a new find.",
                    StructuredArguments.keyValue("component", getComponentName()),
                    e);
            findElement();
            return false;
        } catch (WebDriverException e) {
            log.warn("WebDriverException encountered during click attempt. Retrying.",
                    StructuredArguments.keyValue("component", getComponentName()),
                    e);
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
            log.info("Checking element",
                    StructuredArguments.keyValue("component", getComponentName()));
        } else {
            log.debug("Element is already checked, no action taken",
                    StructuredArguments.keyValue("component", getComponentName()));
        }
    }

    protected void defaultUncheck() {
        toExist().toBeClickable().waitToBe();
        if (getWrappedElement().isSelected()) {
            clickInternal();
            log.info("Unchecking element",
                    StructuredArguments.keyValue("component", getComponentName()));
        } else {
            log.debug("Element is already unchecked, no action taken",
                    StructuredArguments.keyValue("component", getComponentName()));
        }
    }

    protected String defaultGetText() {
        try {
            String text = Optional.ofNullable(getWrappedElement().getText()).orElse("");
            log.debug("Getting text from element",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("text", text));
            return text;
        } catch (StaleElementReferenceException e) {
            String text = Optional.ofNullable(findElement().getText()).orElse("");
            log.debug("Getting text from element (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("text", text));
            return text;
        }
    }

    protected boolean defaultGetDisabledAttribute() {
        try {
            var valueAttr = Optional.ofNullable(getAttribute("disabled")).orElse("false");
            boolean isDisabled = valueAttr.equalsIgnoreCase("true");
            log.debug("Getting 'disabled' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("isDisabled", isDisabled));
            return isDisabled;
        } catch (StaleElementReferenceException e) {
            var valueAttr = Optional.ofNullable(findElement().getAttribute("disabled")).orElse("false");
            boolean isDisabled = valueAttr.equalsIgnoreCase("true");
            log.debug("Getting 'disabled' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("isDisabled", isDisabled));
            return isDisabled;
        }
    }

    protected String defaultGetInnerHtmlAttribute() {
        try {
            String innerHtml = Optional.ofNullable(getAttribute("innerHTML")).orElse("");
            log.debug("Getting 'innerHTML' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("innerHtml", innerHtml));
            return innerHtml;
        } catch (StaleElementReferenceException e) {
            String innerHtml = Optional.ofNullable(findElement().getAttribute("innerHTML")).orElse("");
            log.debug("Getting 'innerHTML' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("innerHtml", innerHtml));
            return innerHtml;
        }
    }

    @SneakyThrows
    protected String defaultGetHref() {
        try {
            String href = unescapeHtml4(URLDecoder.decode(Optional.ofNullable(getAttribute("href")).orElse(""),
                    StandardCharsets.UTF_8));
            log.debug("Getting 'href' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("href", href));
            return href;
        } catch (StaleElementReferenceException e) {
            String href = unescapeHtml4(URLDecoder.decode(Optional.ofNullable(findElement().getAttribute("href")).orElse(""),
                    StandardCharsets.UTF_8));
            log.debug("Getting 'href' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("href", href));
            return href;
        }
    }

    protected String defaultGetAriaLabel() {
        try {
            String ariaLabel = Optional.ofNullable(getAttribute("aria-label")).orElse("");
            log.debug("Getting 'aria-label' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("ariaLabel", ariaLabel));
            return ariaLabel;
        } catch (StaleElementReferenceException e) {
            String ariaLabel = Optional.ofNullable(findElement().getAttribute("aria-label")).orElse("");
            log.debug("Getting 'aria-label' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("ariaLabel", ariaLabel));
            return ariaLabel;
        }
    }

    protected String defaultGetRelAttribute() {
        try {
            String rel = Optional.ofNullable(getAttribute("rel")).orElse("");
            log.debug("Getting 'rel' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("rel", rel));
            return rel;
        } catch (StaleElementReferenceException e) {
            String rel = Optional.ofNullable(findElement().getAttribute("rel")).orElse("");
            log.debug("Getting 'rel' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("rel", rel));
            return rel;
        }
    }

    protected String defaultGetTargetAttribute() {
        try {
            String target = Optional.ofNullable(getAttribute("target")).orElse("");
            log.debug("Getting 'target' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("target", target));
            return target;
        } catch (StaleElementReferenceException e) {
            String target = Optional.ofNullable(findElement().getAttribute("target")).orElse("");
            log.debug("Getting 'target' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("target", target));
            return target;
        }
    }

    protected String defaultGetValue() {
        try {
            String value = Optional.ofNullable(getAttribute("value")).orElse("");
            log.debug("Getting 'value' attribute",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("value", value));
            return value;
        } catch (StaleElementReferenceException e) {
            String value = Optional.ofNullable(findElement().getAttribute("value")).orElse("");
            log.debug("Getting 'value' attribute (stale reference handled)",
                    StructuredArguments.keyValue("component", getComponentName()),
                    StructuredArguments.keyValue("value", value));
            return value;
        }
    }

    protected void defaultSelectByText(String value) {
        log.info("Selecting element by visible text",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("textToSelect", value));
        new Select(getWrappedElement()).selectByVisibleText(value);
    }

    protected void defaultSetText(String value) {
        log.info("Setting text on element",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("textToSet", value));
        getWrappedElement().clear();
        getWrappedElement().sendKeys(value);
    }

    protected void defaultSelectByIndex(int value) {
        log.info("Selecting element by index",
                StructuredArguments.keyValue("component", getComponentName()),
                StructuredArguments.keyValue("indexToSelect", value));
        new Select(getWrappedElement()).selectByIndex(value);
    }
}