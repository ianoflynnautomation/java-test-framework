package solutions.bjjeire.selenium.web.pages;

import solutions.bjjeire.selenium.web.components.custom.grid.ErrorState;
import solutions.bjjeire.selenium.web.components.custom.grid.NoDataState;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.NavigationService;

public abstract class ListPageBase extends WebPage {

  public ListPageBase(
      NavigationService navigationService, ComponentCreateService componentCreateService) {
    super(navigationService, componentCreateService);
  }

  public ErrorState errorStateContainer() {
    return create().byDataTestId(ErrorState.class, "error-state");
  }

  public NoDataState noDataStateContainer() {
    return create().byDataTestId(NoDataState.class, "no-data-state");
  }

  public ListPageBase assertNoDataInList() {
    NoDataState noDataState = noDataStateContainer();
    noDataState.toBeVisible().waitToBe();

    return this;
  }

  public ListPageBase assertErrorInList() {
    ErrorState errorState = errorStateContainer();
    errorState.stateTitle().validateTextIs("Error Loading Data");
    errorState.messageLine1().validateTextIs("Network Error");

    return this;
  }
}
