package solutions.bjjeire.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties()
public class TestUsersConfig {

  private Map<String, User> users = new HashMap<>();

  @Data
  public static class User {
    private String userId;
    private String role;
  }

  public User getUser(String key) {
    return Optional.ofNullable(users.get(key))
        .orElseThrow(
            () -> new IllegalArgumentException("Test user not found in configuration: " + key));
  }
}
