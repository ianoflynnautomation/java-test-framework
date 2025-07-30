package solutions.bjjeire.api.http.auth;

import org.springframework.http.HttpHeaders;

@FunctionalInterface
public interface Authentication {

    void apply(HttpHeaders headers);
}