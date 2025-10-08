package solutions.bjjeire.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class ApiKeyAuth implements Authentication {

  private final String apiKey;
  private final String headerName;

  @Override
  public void apply(HttpHeaders headers) {
    headers.set(this.headerName, this.apiKey);
  }
}
