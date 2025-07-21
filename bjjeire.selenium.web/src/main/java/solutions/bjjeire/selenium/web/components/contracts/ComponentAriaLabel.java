package solutions.bjjeire.selenium.web.components.contracts;


public interface ComponentAriaLabel extends BjjEireComponent {
    String getAriaLabel();

    default void validateAriaLabelIs(String value) {
        validator().attribute(this::getAriaLabel, "aria-label").is(value);
    }

    default void validateAriaLabelContains(String value) {
        validator().attribute(this::getAriaLabel, "aria-label").contains(value);
    }

    default void validateAriaLabelIsSet() {
        validator().attribute(() -> !getAriaLabel().isEmpty(), "aria-label").isTrue();
    }
}