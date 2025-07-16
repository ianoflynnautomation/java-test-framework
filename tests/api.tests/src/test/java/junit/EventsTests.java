package junit;

import Data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import solutions.bjjeire.api.infrastructure.junit.ApiTest;
import solutions.bjjeire.api.validation.ResponseAsserter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class EventsTests extends ApiTest {

    @Test
    public void testCreatePostWithBjjEvent() throws JsonProcessingException{
        // Arrange

        ResponseAsserter<GenerateTokenResponse> tokenAsserter = when()
                .withQueryParams(Map.of(
                        "userId", "dev-user@example.com",
                        "role", "Admin"
                ))
                .get("/generate-token", GenerateTokenResponse.class)
                .then()
                .hasStatusCode(200);

        String token = tokenAsserter.getData()
                .map(GenerateTokenResponse::token)
                .orElseThrow(() -> new IllegalStateException("Token could not be retrieved from auth response."));


        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();

        // Act
        when()
                .withAuthToken(token)
                .body(command)
                .post("/api/bjjevent", CreateBjjEventResponse.class)
                .then()
                .hasStatusCode(201)
                .and().bodySatisfies(response -> {
                    // Assert
                    assertNotNull(response, "Response should not be null");
                    assertNotNull(response.data(), "Response data should not be null");

                    BjjEvent responseData = response.data();

                    assertNotNull(responseData.id());
                    assertEquals(command.data().name(), responseData.name());
                    assertEquals(command.data().description(), responseData.description());
                    assertEquals(command.data().type(), responseData.type());
                    assertEquals(command.data().organiser(), responseData.organiser());
                    assertEquals(command.data().location(), responseData.location());
                    assertEquals(command.data().pricing(), responseData.pricing());
                });
    }
}
