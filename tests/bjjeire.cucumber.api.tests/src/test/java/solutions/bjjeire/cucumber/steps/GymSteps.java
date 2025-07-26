package solutions.bjjeire.cucumber.steps;

import com.github.javafaker.Faker;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.cucumber.context.ScenarioContext;

public class GymSteps {

    private static final Logger logger = LoggerFactory.getLogger(GymSteps.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ScenarioContext context;
    @Autowired
    private ApiSettings settings;
    @Autowired
    private Faker faker;

    @And("the Gym API is available")
    public void theGymAPIIsAvailable() {

    }

    @Given("I have valid details for a new Bjj Gym named {string}")
    public void iHaveValidDetailsForANewBjjGymNamed(String arg0) {

    }

    @When("I create the Bjj Gym")
    public void iCreateTheBjjGym() {

    }

    @Then("the gym is created successfully")
    public void theGymIsCreatedSuccessfully() {

    }

    @And("the gym details include:")
    public void theGymDetailsInclude() {
    }
}
