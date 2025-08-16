package junit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.api.services.GymService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BJJ Gyms API")
@RequiredArgsConstructor
class GymTests extends ApiTestBase {

    @Autowired private  GymService gymService;
    @Autowired private AuthService authService;
    private String authToken;

    @BeforeEach
    void setup() {
        this.authToken = authService.authenticateAsAdmin();
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
            ApiResponse apiResponse = gymService.createGym(authToken, command).block();

            // Assert
            assertAll("Verify successful gym creation",
                    () -> apiResponse.should().statusCode(201),
                    () -> apiResponse.should().bodySatisfies(CreateGymResponse.class, responseBody -> {
                        assertEquals(gymToCreate.name(), responseBody.data().name(), "Gym name should match the request");

                        registerForCleanup(() -> gymService.deleteGym(authToken, responseBody.data().id()));
                    })
            );
        }
    }
}
