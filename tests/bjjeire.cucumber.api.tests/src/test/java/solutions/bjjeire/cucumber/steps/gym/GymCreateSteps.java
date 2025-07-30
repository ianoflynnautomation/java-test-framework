package solutions.bjjeire.cucumber.steps.gym;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.GymApiActions;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.steps.CucumberTestBase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class GymCreateSteps extends CucumberTestBase {

    @Autowired private ScenarioContext scenarioContext;
    @Autowired private GymApiActions gymApi;

    private Gym createdGym;

    @Given("I have valid details for a new Bjj Gym named {string}")
    public void iHaveValidDetailsForANewBjjGymNamed(String gymName) {
        Gym gymToCreate = GymFactory.createGym(builder -> builder.name(gymName));
        scenarioContext.setRequestPayload(new CreateGymCommand(gymToCreate));
    }

    @When("I create the Bjj Gym")
    public void iCreateTheBjjGym() {
        CreateGymCommand command = (CreateGymCommand) scenarioContext.getRequestPayload();
        CreateGymResponse response = gymApi.createGym(scenarioContext.getAuthToken(), command.data());
        this.createdGym = response.data();
        scenarioContext.getCreatedEntities().add(this.createdGym);
    }

    @Then("the gym is created successfully")
    public void theGymIsCreatedSuccessfully() {
        assertNotNull(this.createdGym, "Gym was not created successfully.");
        assertNotNull(this.createdGym.id(), "Created gym ID is null.");
    }

    @And("the gym details include:")
    public void theGymDetailsInclude(DataTable dataTable) {
        assertNotNull(this.createdGym, "Cannot verify details, no gym found in context.");
        Map<String, String> expectedDetails = dataTable.asMap();
        expectedDetails.forEach((fieldName, expectedValue) -> {
            switch (fieldName) {
                case "Name":
                    assertEquals(expectedValue, this.createdGym.name(), "Validation failed for Name.");
                    break;
                case "Location":
                    assertEquals(expectedValue, this.createdGym.county().toString(), "Validation failed for Location.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + fieldName);
            }
        });
    }
}