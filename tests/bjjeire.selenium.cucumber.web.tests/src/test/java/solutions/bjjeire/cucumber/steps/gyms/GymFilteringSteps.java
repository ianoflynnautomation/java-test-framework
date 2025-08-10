package solutions.bjjeire.cucumber.steps.gyms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.context.TestDataContext;
import solutions.bjjeire.cucumber.hooks.TestDataLifecycleHook;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

@Slf4j
@RequiredArgsConstructor
public class GymFilteringSteps {

    private final GymsPage gymsPage;
    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final TestDataContext testDataContext;

    @Data
    public static class GymDataRow {
        private String name;
        private County county;
    }

    @Given("the following BJJ gyms exist:")
    public void the_following_bjj_gyms_exist(DataTable dataTable) {
        List<GymDataRow> dataRows = dataTable.asList(GymDataRow.class);

        List<Gym> gymsToCreate = dataRows.stream()
                .map(row -> GymFactory.createGym(builder -> builder
                        .name(row.getName())
                        .county(row.getCounty())))
                .collect(Collectors.toList());

        String authToken = scenarioContext.getAuthToken();
        List<String> createdIds = testDataManager.seed(gymsToCreate, authToken);

        testDataContext.addEntityIds(Gym.class, createdIds);
        log.info("Seeded {} BJJ gym(s) for the test.", createdIds.size());
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