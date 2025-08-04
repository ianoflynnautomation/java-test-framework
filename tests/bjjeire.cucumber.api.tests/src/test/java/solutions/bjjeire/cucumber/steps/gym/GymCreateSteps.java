package solutions.bjjeire.cucumber.steps.gym;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.services.GymService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.api.validation.ResponseValidatorFactory;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.TestContext;

public class GymCreateSteps {

    @Autowired
    private TestContext testContext;
    @Autowired
    private GymService gymService;
    @Autowired // Inject the ResponseValidatorFactory
    private ResponseValidatorFactory responseValidator;

    @Given("a new BJJ gym has been prepared")
    public void aNewGymHasBeenPrepared() {
        testContext.setRequestPayload(new CreateGymCommand(GymFactory.getValidGym()));
    }

    @When("the Admin adds the new BJJ gym")
    public void adminAddsTheNewGym() {
        CreateGymCommand command = (CreateGymCommand) testContext.getRequestPayload();
        ApiResponse response = gymService.createGym(testContext.getAuthToken(), command).block();
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 201) {
            Gym createdGym = response.as(CreateGymResponse.class).data();
            testContext.addEntityForCleanup(createdGym);
        }
    }

    @Then("the gym should be successfully added")
    public void theGymShouldBeSuccessfullyAdded() {
        // Corrected: Use the factory to create a ResponseValidator instance
        responseValidator.validate(testContext.getLastResponse()).statusCode(201);
    }

}
