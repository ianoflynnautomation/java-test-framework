package solutions.bjjeire.cucumber.steps.gym;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(GymCreateSteps.class);

    @Autowired
    private ScenarioContext context;

    @Given("I have valid details for a new Bjj Gym named {string}")
    public void iHaveValidDetailsForANewBjjGymNamed(String gymName) {
        Gym gymToCreate = GymFactory.createGym(builder -> builder.name(gymName));
        context.setRequestPayload(new CreateGymCommand(gymToCreate));
        context.setGymName(gymName);
    }

    @When("I create the Bjj Gym")
    public void iCreateTheBjjGym() {
        ValidatableResponse response = given()
                .withAuthToken(context.getAuthToken())
                .withBody(context.getRequestPayload())
                .post("/api/gym");
        context.setValidatableResponse(response);
    }

    @Then("the gym is created successfully")
    public void theGymIsCreatedSuccessfully() {
        ValidatableResponse response = context.getValidatableResponse();
        assertNotNull(response, "ValidatableResponse was not found in the context.");

        response.hasStatusCode(201);
        Gym createdGym = response.as(CreateGymResponse.class).data();
        context.setCreatedGym(createdGym);

        final String gymId = createdGym.id();
        context.addCleanupAction(() -> {
            logger.info("CLEANUP: Deleting gym with ID: {}", gymId);
            given().withAuthToken(context.getAuthToken())
                    .delete("/api/gym/" + gymId)
                    .then().hasStatusCode(204);
        });
    }

    @And("the gym details include:")
    public void theGymDetailsInclude(DataTable dataTable) {
        Gym createdGym = context.getCreatedGym();
        assertNotNull(createdGym, "Cannot verify details, no gym found in context.");
        Map<String, String> expectedDetails = dataTable.asMap(String.class, String.class);
        expectedDetails.forEach((fieldName, expectedValue) -> {
            switch (fieldName) {
                case "Name":
                    assertEquals(expectedValue, createdGym.name(), "Validation failed for Name.");
                    break;
                case "Location":
                    assertEquals(expectedValue, createdGym.county().toString(), "Validation failed for Location.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + fieldName);
            }
        });
    }
}