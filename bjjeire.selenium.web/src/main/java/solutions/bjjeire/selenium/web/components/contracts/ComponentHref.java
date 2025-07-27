package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentHref extends BjjEireComponent {

    String getHref();

    default void validateHrefIs(String value) {
        validator().attribute(this::getHref, "href").is(value);
    }

    default void validateHrefContains(String value) {
        validator().attribute(this::getHref, "href").contains(value);
    }

    default void validateHrefNotContains(String value) {
        validator().attribute(this::getHref, "href").notContains(value);
    }

    default void validateHrefIsSet() {
        validator().attribute(() -> !getHref().isEmpty(), "href").isTrue();
    }

    default void validateHrefNotSet() {
        validator().attribute(() -> getHref().isEmpty(), "href").isTrue();
    }
}
