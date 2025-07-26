package solutions.bjjeire.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.cucumber.context.BaseContext;
import solutions.bjjeire.cucumber.context.GymContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

public class GymFilteringSteps {

    @Autowired private GymsPage gymsPage;
    @Autowired private TestDataManager testDataManager;
    @Autowired private BaseContext baseContext;
    @Autowired private GymContext gymContext;

    @Given("the following BJJ gyms exist:")
    public void the_following_bjj_gyms_exist(DataTable dataTable) {
        // TODO: Implement gym data creation using a factory, similar to events
        //        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
//        List<Gym> gymsToCreate = new ArrayList<>();
//
//        for (Map<String, String> columns : rows) {
//            // Assumes a factory and builder pattern for creating gym objects
//            Gym gym = BjjGymFactory.createBjjGym(builder -> {
//                String name = columns.get("Name");
//                String county = columns.get("County");
//
//                if (name != null) {
//                    builder.name(name);
//                }
//                if (county != null) {
//                    builder.county(County.valueOf(county.replace(" ", "")));
//                }
//            });
//            gymsToCreate.add(gym);
//        }
//
//        String authToken = testContext.getAuthToken();
//        // Assumes a `seedGyms` method exists in your TestDataManager
//        List<String> createdIds = testDataManager.seedGyms(gymsToCreate, authToken);
//        // Assumes a method to store gym IDs exists in your TestContext
//        testContext.addCreatedGymIds(createdIds);
    }


    @When("I filter gyms by county {string}")
    public void i_filter_gyms_by_county(String county) {
        gymsPage.selectCounty(county);
    }

    @Then("I should see gyms only for the county {string}")
    public void i_should_see_gyms_only_for_the_county(String expectedCounty) {
        gymsPage.assertAllGymsMatchCountyFilter(expectedCounty);
    }

    @Then("the gyms list should be empty")
    public void the_gyms_list_should_be_empty() {
        gymsPage.assertNoDataInList();
    }
}