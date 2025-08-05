package solutions.bjjeire.selenium.web.services;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.selenium.web.components.WebComponent;
import solutions.bjjeire.selenium.web.configuration.WebSettings;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ComponentValidationService {
    private static final Logger log = LoggerFactory.getLogger(ComponentValidationService.class);

    private final DriverService driverService;
    private final WebSettings webSettings;
    private WebComponent component;
    private String attributeName;

    public ComponentValidationService(DriverService driverService, WebSettings webSettings) {
        this.driverService = driverService;
        this.webSettings = webSettings;
    }

    public void setComponent(WebComponent component) {
        this.component = component;
        log.trace("Validation service configured for component",
                StructuredArguments.keyValue("componentName", component.getComponentName()));
    }

    public <T> AttributeValidation<T> attribute(Supplier<T> attributeSupplier, String attributeName) {
        this.attributeName = attributeName;
        return new AttributeValidation<>(attributeSupplier);
    }

    public class AttributeValidation<T> {
        private final Supplier<T> actualValueSupplier;

        public AttributeValidation(Supplier<T> actualValueSupplier) {
            this.actualValueSupplier = actualValueSupplier;
        }

        public void is(T expectedValue) {
            waitUntil(() -> {
                T actual = actualValueSupplier.get();
                return actual != null && actual.equals(expectedValue);
            }, String.valueOf(expectedValue), "be", () -> String.valueOf(actualValueSupplier.get()));
        }

        public void isNull() {
            waitUntil(() -> actualValueSupplier.get() == null, "null", "be",
                    () -> String.valueOf(actualValueSupplier.get()));
        }

        public void isTrue() {
            waitUntil(() -> Boolean.TRUE.equals(actualValueSupplier.get()), "true", "be",
                    () -> String.valueOf(actualValueSupplier.get()));
        }

        public void isFalse() {
            waitUntil(() -> Boolean.FALSE.equals(actualValueSupplier.get()), "false", "be",
                    () -> String.valueOf(actualValueSupplier.get()));
        }

        public void contains(String expectedValue) {
            waitUntil(() -> {
                T actual = actualValueSupplier.get();
                return actual instanceof String && ((String) actual).contains(expectedValue);
            }, expectedValue, "contain", () -> String.valueOf(actualValueSupplier.get()));
        }

        public void notContains(String unexpectedValue) {
            waitUntil(() -> {
                T actual = actualValueSupplier.get();
                return actual instanceof String && !((String) actual).contains(unexpectedValue);
            }, unexpectedValue, "not contain", () -> String.valueOf(actualValueSupplier.get()));
        }
    }

    private void waitUntil(BooleanSupplier condition, String expectedValue, String verb,
            Supplier<String> actualValueSupplier) {
        var timeoutSettings = webSettings.getTimeoutSettings();
        var validationTimeout = timeoutSettings.getValidationsTimeout();
        var sleepInterval = timeoutSettings.getSleepInterval();

        FluentWait<WebDriver> wait = new FluentWait<>(driverService.getWrappedDriver())
                .withTimeout(Duration.ofSeconds(validationTimeout))
                .pollingEvery(Duration.ofSeconds(sleepInterval > 0 ? sleepInterval : 1));

        try {
            wait.until(driver -> {
                component.findElement();
                return condition.getAsBoolean();
            });

            log.info("Validation successful",
                    StructuredArguments.keyValue("componentName", component.getComponentName()),
                    StructuredArguments.keyValue("attributeName", attributeName),
                    StructuredArguments.keyValue("expectedVerb", verb),
                    StructuredArguments.keyValue("expectedValue", expectedValue));

        } catch (TimeoutException ex) {
            String actualValue = actualValueSupplier.get();

            log.error("Validation failed",
                    StructuredArguments.keyValue("componentClass", component.getComponentClass().getSimpleName()),
                    StructuredArguments.keyValue("findStrategy", component.getFindStrategy()),
                    StructuredArguments.keyValue("attributeName", attributeName),
                    StructuredArguments.keyValue("expectedVerb", verb),
                    StructuredArguments.keyValue("expectedValue", expectedValue),
                    StructuredArguments.keyValue("actualValue", actualValue),
                    StructuredArguments.keyValue("currentUrl", driverService.getWrappedDriver().getCurrentUrl()),
                    ex);

            String errorMessage = """
                Validation failed for component: %s (%s)
                Attribute: %s
                Expected to %s: "%s"
                But actual was: "%s"
                On URL: %s
                """.formatted(
                    component.getComponentClass().getSimpleName(),
                    component.getFindStrategy(),
                    attributeName,
                    verb,
                    expectedValue,
                    actualValue,
                    driverService.getWrappedDriver().getCurrentUrl()
            );

            throw new AssertionError(errorMessage, ex);
        }
    }
}