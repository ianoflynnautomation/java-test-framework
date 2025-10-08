package solutions.bjjeire.selenium.web.configuration;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class GridSettings {

  @NotBlank(message = "Grid provider name cannot be blank.")
  private String providerName;

  private String optionsName;

  @NotBlank(message = "Grid URL cannot be blank.")
  @URL(message = "Grid URL must be a valid URL.")
  private String url;

  private List<HashMap<String, Object>> arguments;
}
