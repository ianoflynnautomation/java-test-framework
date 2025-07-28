package solutions.bjjeire.cucumber.steps.gym;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.actions.GymApiActions;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class GymCreateSteps {
    private static final Logger logger = LoggerFactory.getLogger(GymCreateSteps.class);
    @Autowired private ApplicationContext applicationContext;
    @Autowired private ScenarioContext context;
    private final GymApiActions gymApi = new GymApiActions();
    private TestClient testClient() { return applicationContext.getBean(TestClient.class); }

    @Given("I have valid details for a new Bjj Gym named {string}")
    public void iHaveValidDetailsForANewBjjGymNamed(String gymName) {
        Gym gymToCreate = GymFactory.createGym(builder -> builder.name(gymName));
        context.setRequestPayload(gymToCreate);
    }

    @When("I create the Bjj Gym")
    public void iCreateTheBjjGym() {
        // FIX: Wrap the Gym object in a CreateGymCommand
        Gym gymPayload = (Gym) context.getRequestPayload();
        CreateGymCommand command = new CreateGymCommand(gymPayload);

        ResponseAsserter asserter = testClient()
                .withAuthToken(context.getAuthToken())
                .body(command) // Send the command object
                .post("/api/gym");
        context.setResponseAsserter(asserter);
    }

    @Then("the gym is created successfully")
    public void theGymIsCreatedSuccessfully() {
        ResponseAsserter asserter = context.getResponseAsserter();
        assertNotNull(asserter, "ResponseAsserter was not found in the context.");

        asserter.hasStatusCode(201);
        Gym createdGym = asserter.as(CreateGymResponse.class).data();
        context.setCreatedGym(createdGym);

        final String gymId = createdGym.id();
        context.addCleanupAction(client -> {
            logger.info("CLEANUP: Deleting gym with ID: {}", gymId);
            client.withAuthToken(context.getAuthToken())
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