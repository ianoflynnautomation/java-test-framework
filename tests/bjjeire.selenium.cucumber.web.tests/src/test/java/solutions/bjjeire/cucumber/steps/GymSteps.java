package solutions.bjjeire.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

public class GymSteps {

    @Autowired
    private GymsPage gymsPage;

    @Given("I am on the BJJ app gyms page")
    public void iAmOnTheBJJAppGymsPage() {
        gymsPage.open();
    }

    @Then("I should see gyms only for the county {string}")
    public void iShouldSeeGymsOnlyForTheCounty(String expectedCounty) {
        gymsPage.assertAllGymsMatchCountyFilter(expectedCounty);
    }

    @Then("I should see a message indicating no gyms are available")
    public void iShouldSeeAMessageIndicatingNoGymsAreAvailable() {
        gymsPage.assertNoDataInList();
    }

    @And("the gyms list should be empty")
    public void theGymsListShouldBeEmpty() {
        gymsPage.assertNoDataInList();
    }

    @When("I filter the gyms list by {string} from the dropdown")
    public void iFilterTheGymsListByFromTheDropdown(String county) {
        gymsPage.selectCounty(county);
    }
}
