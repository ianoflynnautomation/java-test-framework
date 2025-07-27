package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentRel extends BjjEireComponent {

    String getRel();

    default void validateRelIs(String value) {
        validator().attribute(this::getRel, "rel").is(value);
    }

    default void validateRelIsSet() {
        validator().attribute(() -> !getRel().isEmpty(), "rel").isTrue();
    }

    default void validateRelNotSet() {
        validator().attribute(() -> getRel().isEmpty(), "rel").isTrue();
    }
}
