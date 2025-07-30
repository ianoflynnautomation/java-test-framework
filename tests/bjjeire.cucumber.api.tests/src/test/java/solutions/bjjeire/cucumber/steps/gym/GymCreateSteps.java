package solutions.bjjeire.cucumber.steps.gym;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.GymApiActions;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.TestState;

public class GymCreateSteps {

    @Autowired
    private TestState testState;
    @Autowired
    private GymApiActions gymApi;

    @Given("a new BJJ gym has been prepared")
    public void aNewBjjGymHasBeenPrepared() {
        testState.setRequestPayload(new CreateGymCommand(GymFactory.getValidGym()));
    }

    @When("Admin adds the new BJJ gym")
    public void adminAndyAddsTheNewBjjGym() {
        CreateGymCommand command = (CreateGymCommand) testState.getRequestPayload();

        ApiResponse response = gymApi.createGym(testState.getAuthToken(), command);
        testState.setLastResponse(response);

        if (response.getStatusCode() == 201) {
            Gym createdGym = response.as(CreateGymResponse.class).data();
            testState.addEntity(createdGym);
        }
    }

    @Then("the gym should be successfully added")
    public void theGymShouldBeSuccessfullyAdded() {
        testState.getLastResponse().then().statusCode(201);
    }
}