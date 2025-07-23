package solutions.bjjeire.selenium.web.pages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.selenium.web.components.custom.grid.ErrorState;
import solutions.bjjeire.selenium.web.components.custom.grid.NoDataState;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.*;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListPageBase extends WebPage{

    @Autowired
    public ListPageBase(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService, ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext, WaitStrategyFactory waitStrategyFactory, NavigationService navigationService, ComponentCreateService componentCreateService) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext, waitStrategyFactory, navigationService, componentCreateService);
    }

    public NoDataState noDataState() {return create().byDataTestId(NoDataState.class, "no-data-state");}

    public ErrorState errorState() {return create().byDataTestId(ErrorState.class, "error-state");}

    @Override
    protected String getUrl() {
        return "";
    }

    public void assertNoDataInList()
    {
        assertTrue(noDataState().isVisible());

    }

    public ListPageBase assertErrorInList()
    {
        assertTrue(errorState().isVisible());

        return this;
    }


}
