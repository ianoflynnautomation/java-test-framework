package solution.bjjeire.selenium.web.components;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import solution.bjjeire.selenium.web.components.enums.ScrollPosition;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solution.bjjeire.selenium.web.findstrategies.*;
import solution.bjjeire.selenium.web.waitstrategies.*;
import solution.bjjeire.selenium.web.infrastructure.DriverService;
import solution.bjjeire.selenium.web.services.BrowserService;
import solution.bjjeire.selenium.web.services.ComponentCreateService;
import solution.bjjeire.selenium.web.services.ComponentWaitService;
import solution.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.core.configuration.ConfigurationService;
import solutions.bjjeire.core.utilities.InstanceFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;


public class WebComponent {

    @Setter(AccessLevel.PROTECTED) private WebElement wrappedElement;
    @Getter @Setter protected WebComponent parentComponent;
    @Getter @Setter private SearchContext parentWrappedElement;
    @Getter @Setter private int elementIndex;
    @Getter @Setter private FindStrategy findStrategy;
    @Getter private final WebDriver wrappedDriver;
    @Getter protected final JavaScriptService javaScriptService;
    @Getter protected final BrowserService browserService;
    @Getter protected final ComponentCreateService componentCreateService;
    @Getter protected final ComponentWaitService componentWaitService;
    private final List<WaitStrategy> waitStrategies;
    private final WebSettings webSettings;

    public WebComponent() {
        waitStrategies = new ArrayList<>();
        webSettings = ConfigurationService.get(WebSettings.class);
        javaScriptService = new JavaScriptService();
        browserService = new BrowserService();
        componentCreateService = new ComponentCreateService();
        componentWaitService = new ComponentWaitService();
        wrappedDriver = DriverService.getWrappedDriver();
    }

    public <ComponentT extends WebComponent> ComponentT as(Class<ComponentT> componentClass) {
        if (this.getClass() == componentClass) return (ComponentT)this;

        var component = InstanceFactory.create(componentClass);

//        if (componentClass != ShadowRoot.class) {
//            component.setWrappedElement(this.wrappedElement);
//        }
        component.setParentComponent(this.parentComponent);
        component.setParentWrappedElement(this.parentWrappedElement);
        component.setFindStrategy(this.findStrategy);
        component.setElementIndex(this.elementIndex);

        return component;
    }

    public WebElement getWrappedElement() {
        try {
            if(wrappedElement != null) {
                wrappedElement.isDisplayed(); // checking if getting property throws exception
                return wrappedElement;
            } else {
                return findElement();
            }
        } catch (ElementNotInteractableException ex ) {
            scrollToVisible();
            return findElement();
        } catch (StaleElementReferenceException ex ) {
            return findElement();
        } catch (WebDriverException ex ) {
            toExist().waitToBe();
            return wrappedElement;
        }
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

    public void scrollToTop() {
        scrollToVisible(getWrappedElement(), false, ScrollPosition.START);
    }

    public void scrollToBottom() {
        scrollToVisible(getWrappedElement(), false, ScrollPosition.END);
    }

//    @Override
    public String getAttribute(String name) {
        return getWrappedElement().getAttribute(name);
    }

    public void ensureState(WaitStrategy waitStrategy) {
        waitStrategies.add(waitStrategy);
    }


    public void hover() {
        Actions actions = new Actions(wrappedDriver);
        actions.moveToElement(getWrappedElement()).build().perform();
    }

    public String getTitle() {
        return getAttribute("title");
    }

    public <TElementType extends WebComponent> TElementType toExist() {
        ToExistWaitStrategy waitStrategy = new ToExistWaitStrategy();
        ensureState(waitStrategy);
        return (TElementType)this;
    }

    public <TElementType extends WebComponent> TElementType toBeVisible() {
        ToBeVisibleWaitStrategy waitStrategy = new ToBeVisibleWaitStrategy();
        ensureState(waitStrategy);
        return (TElementType)this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable() {
        ToBeClickableWaitStrategy waitStrategy = new ToBeClickableWaitStrategy();
        ensureState(waitStrategy);
        return (TElementType)this;
    }

    public <TElementType extends WebComponent> TElementType toBeClickable(long timeoutInterval, long sleepInterval) {
        ToBeClickableWaitStrategy waitStrategy = new ToBeClickableWaitStrategy(timeoutInterval, sleepInterval);
        ensureState(waitStrategy);
        return (TElementType)this;
    }

    public <TElementType extends WebComponent, TWaitStrategy extends WaitStrategy> TElementType to(Class<TWaitStrategy> waitClass, TElementType element) {
        TWaitStrategy waitStrategy = InstanceFactory.create(waitClass);
        element.ensureState(waitStrategy);
        return element;
    }

    public <TComponent extends WebComponent, TFindStrategy extends FindStrategy> TComponent create(Class<TFindStrategy> findStrategyClass, Class<TComponent> componentClass, Object... args) {
        TFindStrategy findStrategy = InstanceFactory.create(findStrategyClass, args);
        return create(componentClass, findStrategy);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //
    // BEST PRACTICE LOCATORS: User-Centric Find Strategies
    //
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * ✅ DO: Finds an element by its ARIA role and, optionally, its accessible name.
     * This is the most preferred way to find an element, as it tests the application
     * the way a screen reader user would navigate it.
     * ✅ DO: Finds a form element by its associated label text.
     * ✅ DO: Finds an input element by its placeholder text.
     * ✅ DO: Finds an element by its visible text content.
     */
    public <TComponent extends WebComponent> TComponent createByRole(Class<TComponent> componentClass, String role, String... name) {
        return create(componentClass, new RoleFindStrategy(role, name));
    }

    public <TComponent extends WebComponent> TComponent createByLabelText(Class<TComponent> componentClass, String labelText) {
        return create(componentClass, new LabelTextFindStrategy(labelText));
    }

    public <TComponent extends WebComponent> TComponent createByPlaceholderText(Class<TComponent> componentClass, String placeholderText) {
        return create(componentClass, new PlaceholderFindStrategy(placeholderText));
    }

    public <TComponent extends WebComponent> TComponent createByText(Class<TComponent> componentClass, String text) {
        return create(componentClass, new TextFindStrategy(text));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //
    // IMPLEMENTATION DETAIL LOCATORS: Use with Caution
    //
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * * ⚠️ USE AS A LAST RESORT: Finds an element by its `data-testid` attribute.
     * * ❌ AVOID: Finds an element by its CSS selector.
     * * ❌ AVOID: Finds an element by its XPath.

     */

    public <TComponent extends WebComponent> TComponent createByDataTestId(Class<TComponent> componentClass, String xpath) {
        return create(componentClass, new DataTestIdFindStrategy(xpath));
    }

    public <TComponent extends WebComponent> TComponent createByCss(Class<TComponent> componentClass, String css) {
        return create(componentClass, new CssFindStrategy(css));
    }

    public <TComponent extends WebComponent> TComponent createByXPath(Class<TComponent> componentClass, String xpath) {
        return create(componentClass, new XPathFindStrategy(xpath));
    }

    public <TComponent extends WebComponent> List<TComponent> createAllByXPath(Class<TComponent> componentClass, String xpath) {
        return createAll(componentClass, new XPathFindStrategy(xpath));
    }

    protected <TComponent extends WebComponent, TFindStrategy extends FindStrategy> TComponent create(Class<TComponent> componentClass, TFindStrategy findStrategy) {
        findElement();

        TComponent component;

//        if (inShadowContext()) {
//            component = ShadowDomService.createInShadowContext(componentClass, this, findStrategy);
//        } else {
            component = InstanceFactory.create(componentClass);
            component.setFindStrategy(findStrategy);
            component.setParentComponent(this);
            component.setParentWrappedElement(this.getWrappedElement());
//        }

        return component;
    }

    protected <TComponent extends WebComponent, TFindStrategy extends FindStrategy> List<TComponent> createAll(Class<TComponent> componentClass, TFindStrategy findStrategy) {
        findElement();

        List<TComponent> componentList = new ArrayList<>();

//        if (inShadowContext()) {
//            componentList = ShadowDomService.createAllInShadowContext(componentClass, this, findStrategy);
//        } else {

           var nativeElements = wrappedElement.findElements(findStrategy.convert());

            for (int i = 0; i < nativeElements.size(); i++) {
                var component = InstanceFactory.create(componentClass);
                component.setFindStrategy(findStrategy);
                component.setElementIndex(i);
                component.setParentWrappedElement(wrappedElement);
                componentList.add(component);
            }
//        }

        return componentList;
    }

    public boolean inShadowContext() {
        var component = this;

        while (component != null && component.getParentComponent() != null) {
            component = component.getParentComponent();
//            if (component instanceof ShadowRoot) return true;
        }

        return false;
    }

    public WebElement findElement() {
        if (waitStrategies.isEmpty()) {
            waitStrategies.add(Wait.to().exist(webSettings.getTimeoutSettings().getElementWaitTimeout(), webSettings.getTimeoutSettings().getSleepInterval()));
        }

        try {
            for (var waitStrategy : waitStrategies) {
                componentWaitService.wait(this, waitStrategy);
            }

            wrappedElement = findNativeElement();
            scrollToMakeElementVisible(wrappedElement);
//            if (webSettings.getWaitUntilReadyOnElementFound()) {
//                browserService.waitForAjax();
//            }

//            if (webSettings.getWaitForAngular()) {
//                browserService.waitForAngular();
//            }

//            addArtificialDelay();

            waitStrategies.clear();
        } catch (Exception ex) {
            var formattedException = String.format("The component: \n" +
                            "     Type: %s" +
                            "  Locator: %s" +
                            "  URL: %s\"%n" +
                            "Was not found on the page or didn't fulfill the specified conditions.%n%n",
                    getComponentClass().getSimpleName(), findStrategy.toString(), getWrappedDriver().getCurrentUrl());
//            Log.error(formattedException);

            throw new NotFoundException(formattedException, ex);
        }

        return wrappedElement;
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
//            Log.info("Click has timed out.  Trying with JS click()...");
//            javaScriptService.execute("arguments[0].click()", findElement());
        }
    }

    private boolean tryClick() {
        try {
            toBeVisible().toBeClickable().findElement().click();
            return true;
        } catch (ElementNotInteractableException e) {
//            Log.error("ElementNotInteractableException found - retrying with scroll.. ");
            scrollToVisible();
            return false;
        } catch (StaleElementReferenceException e) {
//            Log.error("StaleElementReference Exception found - retrying with a new Find... ");
            findElement();
            return false;
        } catch (WebDriverException e) {
//            Log.error("WebDriverException found - trying again... ");
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
            return valueAttr.toLowerCase(Locale.ROOT).equals("true");
        } catch (StaleElementReferenceException e) {
            var valueAttr = Optional.ofNullable(findElement().getAttribute("disabled")).orElse("false");
            return valueAttr.toLowerCase(Locale.ROOT).equals("true");
        }
    }
    protected String defaultGetInnerHtmlAttribute() {
//        if (this instanceof ShadowRoot) {
//            return ShadowDomService.getShadowHtml(this, true);
//        } else if (this.inShadowContext()) {
//            return ShadowDomService.getShadowHtml(this, false);
//        } else {
            try {
                return Optional.ofNullable(getAttribute("innerHTML")).orElse("");
            } catch (StaleElementReferenceException e) {
                return Optional.ofNullable(findElement().getAttribute("innerHTML")).orElse("");
            }
        // }
    }

    @SneakyThrows
    protected String defaultGetHref() {
        try {
            return unescapeHtml4(URLDecoder.decode(Optional.ofNullable(getAttribute("href")).orElse(""), StandardCharsets.UTF_8.name()));
        } catch (StaleElementReferenceException e) {
            return unescapeHtml4(URLDecoder.decode(Optional.ofNullable(findElement().getAttribute("href")).orElse(""), StandardCharsets.UTF_8.name()));
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

    private WebElement findNativeElement() {
        if (parentWrappedElement == null) {
            return wrappedDriver.findElements(findStrategy.convert()).get(elementIndex);
        } else {
            return parentWrappedElement.findElements(findStrategy.convert()).get(elementIndex);
        }
    }

    private void scrollToMakeElementVisible(WebElement wrappedElement) {
        // createBy default scroll down to make the element visible.
        if (webSettings.getAutomaticallyScrollToVisible()) {
            scrollToVisible(wrappedElement, false, ScrollPosition.CENTER);
        }
    }

    protected void defaultSelectByIndex(int value) {
        new Select(getWrappedElement()).selectByIndex(value);
    }

    private void scrollToVisible(WebElement wrappedElement, boolean shouldWait, ScrollPosition scrollPosition) {
        try {
            javaScriptService.execute("arguments[0].scrollIntoView({ block: \"" + scrollPosition.getValue() + "\", behavior: \"instant\", inline: \"nearest\" });", wrappedElement);
            if (shouldWait) {
                Thread.sleep(500);
                toExist().waitToBe();
            }
        } catch (ElementNotInteractableException | InterruptedException | ScriptTimeoutException ex) {
//           DebugInformation.printStackTrace(ex);
        }

    }

//    @Override
    public boolean isVisible() {
        try {
            return getWrappedElement().isDisplayed();
        } catch (StaleElementReferenceException e) {
            wrappedElement = findElement();
            return false;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
