package solutions.bjjeire.cucumber.steps.gyms;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.cucumber.actions.GymActions;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

@Slf4j
@RequiredArgsConstructor
public class GymFilteringSteps {

  private final GymsPage gymsPage;
  private final GymActions gymActions;

  @Given("the following BJJ gyms exist:")
  public void the_following_bjj_gyms_exist(DataTable dataTable) {
    gymActions.createGyms(dataTable);
  }

  @Given("I can access gyms")
  public void iCanAccessGyms() {
    gymsPage.open();
  }

  @When("I search gyms by county {string}")
  public void i_filter_gyms_by_county(String county) {
    gymsPage.selectCounty(county);
  }

  @Then("I should only see gyms for county {string}")
  public void i_should_see_gyms_only_for_the_county(String expectedCounty) {
    gymsPage.assertAllGymsMatchCountyFilter(expectedCounty);
  }

  @Then("I should not see any gyms")
  public void the_gyms_list_should_be_empty() {
    gymsPage.assertNoDataInList();
  }
}
