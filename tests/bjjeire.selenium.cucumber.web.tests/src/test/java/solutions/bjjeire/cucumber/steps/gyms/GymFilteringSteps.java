package solutions.bjjeire.cucumber.steps.gyms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.GymContext;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

public class GymFilteringSteps {

    private static final Logger log = LoggerFactory.getLogger(GymFilteringSteps.class);

    private final GymsPage gymsPage;
    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final GymContext gymContext;

    public GymFilteringSteps(GymsPage gymsPage, TestDataManager testDataManager, ScenarioContext scenarioContext,
            GymContext gymContext) {
        this.gymsPage = gymsPage;
        this.testDataManager = testDataManager;
        this.scenarioContext = scenarioContext;
        this.gymContext = gymContext;
    }

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

        String authToken = scenarioContext.getAuthToken();
        List<String> createdIds = testDataManager.seedGyms(gymsToCreate, authToken);
        gymContext.addAllCreatedGymIds(createdIds);
        log.debug("Created {} BJJ gym(s) for the test.", createdIds.size());

    }

    @When("I search gyms by county {string}")
    public void i_filter_gyms_by_county(String county) {
        gymsPage.open();
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