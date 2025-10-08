package solutions.bjjeire.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class BearerTokenAuth implements Authentication {

  private final String token;

  @Override
  public void apply(HttpHeaders headers) {
    if (token != null && !token.isBlank()) {
      headers.setBearerAuth(token);
    }
  }
}
