package junit;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.auth.BearerTokenAuth;
import solutions.bjjeire.api.client.GymsApiClient;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;

@DisplayName("BJJ Gyms API")
@RequiredArgsConstructor
class GymTests extends ApiTestBase {

  @Autowired private GymsApiClient gymsApiClient;
  @Autowired private AuthService authService;
  private String authToken;

  @BeforeEach
  void setup() {
    this.authToken = authService.getTokenFor("admin").block();
  }

  @Nested
  @DisplayName("Create Gym (POST /api/gyms)")
  class CreateGymScenarios {

    @Test
    @DisplayName("Should create a gym successfully with valid details")
    void createGym_withValidData_shouldReturn201() {
      // Arrange
      Gym gymToCreate = GymFactory.getValidGym();
      CreateGymCommand command = new CreateGymCommand(gymToCreate);

      // Act
      ApiResponse apiResponse =
          gymsApiClient.createGym(new BearerTokenAuth(authToken), command).block();

      // Assert
      apiResponse
          .should()
          .isCreated()
          .and()
          .bodySatisfies(
              CreateGymResponse.class,
              responseBody -> {
                assertNotNull(responseBody.data().id(), "Gym ID should not be null");

                org.assertj.core.api.Assertions.assertThat(responseBody.data())
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(gymToCreate);

                registerForCleanup(
                    () ->
                        gymsApiClient
                            .deleteGym(new BearerTokenAuth(authToken), responseBody.data().id())
                            .block());
              });
    }
  }
}
