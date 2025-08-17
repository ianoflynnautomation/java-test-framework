package solutions.bjjeire.api.auth;

import org.springframework.http.HttpHeaders;

@FunctionalInterface
public interface Authentication {

    void apply(HttpHeaders headers);
}