package solutions.bjjeire.cucumber.steps.gyms;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.BaseContext;
import solutions.bjjeire.cucumber.context.GymContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GymFilteringSteps {

    private static final Logger log = LoggerFactory.getLogger(GymFilteringSteps.class);

    @Autowired
    private GymsPage gymsPage;
    @Autowired
    private TestDataManager testDataManager;
    @Autowired
    private BaseContext baseContext;
    @Autowired
    private GymContext gymContext;

    @Given("the following BJJ gyms exist:")
    public void the_following_bjj_gyms_exist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        List<Gym> gymsToCreate = new ArrayList<>();

        for (Map<String, String> columns : rows) {
            Gym gym = GymFactory.createGym(builder -> {
                String name = columns.get("Name");
                String county = columns.get("County");

                if (name != null) {
                    builder.name(name);
                }
                if (county != null) {
                    builder.county(County.valueOf(county.replace(" ", "")));
                }
            });
            gymsToCreate.add(gym);
        }

        String authToken = baseContext.getAuthToken();
        List<String> createdIds = testDataManager.seedGyms(gymsToCreate, authToken);
        gymContext.addAllCreatedGymIds(createdIds);
        log.debug("Created {} BJJ gym(s) for the test.", createdIds.size());
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