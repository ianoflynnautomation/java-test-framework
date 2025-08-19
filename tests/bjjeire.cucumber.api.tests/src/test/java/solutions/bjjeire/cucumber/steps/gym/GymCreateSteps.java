package solutions.bjjeire.cucumber.steps.gym;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.auth.BearerTokenAuth;
import solutions.bjjeire.api.client.GymsApiClient;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.TestContext;

@Slf4j
@RequiredArgsConstructor
public class GymCreateSteps {

    private final TestContext testContext;
    @Autowired private GymsApiClient gymsApiClient;


    @Given("a new BJJ gym has been prepared")
    public void aNewGymHasBeenPrepared() {
        testContext.setRequestPayload(new CreateGymCommand(GymFactory.getValidGym()));
    }

    @When("the Admin adds the new BJJ gym")
    public void adminAddsTheNewGym() {
        CreateGymCommand command = (CreateGymCommand) testContext.getRequestPayload();
        ApiResponse response = gymsApiClient.createGym(new BearerTokenAuth(testContext.getAuthToken()), command).block();
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 201) {
            Gym createdGym = response.as(CreateGymResponse.class).data();
            testContext.addEntityForCleanup(createdGym);
        }
    }

    @Then("the gym should be successfully added")
    public void theGymShouldBeSuccessfullyAdded() {
        ApiResponse response = testContext.getLastResponse();
        response.should().isCreated();
    }

}
