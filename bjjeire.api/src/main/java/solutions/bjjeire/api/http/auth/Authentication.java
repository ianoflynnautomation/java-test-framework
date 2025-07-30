package solutions.bjjeire.api.http.auth;

import org.springframework.http.HttpHeaders;

@FunctionalInterface
public interface Authentication {
    /**
     * Applies the authentication details to the given HttpHeaders.
     *
     * @param headers The headers of the outgoing request.
     */
    void apply(HttpHeaders headers);
}