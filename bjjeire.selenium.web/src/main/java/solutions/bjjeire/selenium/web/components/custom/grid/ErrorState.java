package solutions.bjjeire.selenium.web.components.custom.grid;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.selenium.web.components.Button;
import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Paragraph;
import solutions.bjjeire.selenium.web.components.WebComponent;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

@Component
@Scope("prototype")
public class ErrorState extends WebComponent {

    public ErrorState(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
            ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory);
    }

    public Heading stateTitle() {
        return createByDataTestId(Heading.class, "error-state-title");
    }

    public Paragraph messageLine1() {
        return createByDataTestId(Paragraph.class, "error-state-message-line1");
    }

    public Paragraph messageLine2() {
        return createByDataTestId(Paragraph.class, "error-state-message-line2");
    }

    @SuppressWarnings("unused")
    private Button retryButton() {
        return createByDataTestId(Button.class, "error-state-button");
    }

}
