package solutions.bjjeire.selenium.web.configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
public class Admin {

    @NotBlank(message = "Admin user cannot be blank.")
    private String user;

    @NotBlank(message = "Admin role cannot be blank.")
    private String role;
}
