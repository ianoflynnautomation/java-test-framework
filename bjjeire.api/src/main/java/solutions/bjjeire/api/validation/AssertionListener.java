package solutions.bjjeire.api.validation;

/**
 * A listener for assertion events, allowing for cross-cutting concerns
 * like logging or custom reporting for each assertion made.
 */
public interface AssertionListener {
    void onAssertion(ApiAssertEvent event);
}
