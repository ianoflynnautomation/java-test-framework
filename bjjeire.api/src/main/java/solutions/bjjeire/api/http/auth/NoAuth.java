package solutions.bjjeire.api.http.auth;

import org.springframework.http.HttpHeaders;

public class NoAuth implements Authentication {
    @Override
    public void apply(HttpHeaders headers) {

    }
}