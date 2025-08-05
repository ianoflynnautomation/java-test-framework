package solutions.bjjeire.selenium.web.pages;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.context.ApplicationContext;

import solutions.bjjeire.selenium.web.components.custom.grid.ErrorState;
import solutions.bjjeire.selenium.web.components.custom.grid.NoDataState;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

public class ListPageBase extends WebPage {

    public ListPageBase(
            NavigationService navigationService, ComponentCreateService componentCreateService) {
        super(navigationService, componentCreateService);
    }

    public NoDataState noDataState() {
        return create().byDataTestId(NoDataState.class, "no-data-state");
    }

    public ErrorState errorState() {
        return create().byDataTestId(ErrorState.class, "error-state");
    }

    @Override
    protected String getUrl() {
        return "";
    }

    public void assertNoDataInList() {
        assertTrue(noDataState().isVisible());

    }

    public ListPageBase assertErrorInList() {
        assertTrue(errorState().isVisible());

        return this;
    }

}
