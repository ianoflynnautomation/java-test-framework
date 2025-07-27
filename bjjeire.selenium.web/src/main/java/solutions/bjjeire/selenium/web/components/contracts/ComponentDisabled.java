package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentDisabled extends BjjEireComponent {

    boolean isDisabled();

    default void validateIsDisabled() {
        validator().attribute(this::isDisabled, "disabled").isTrue();
    }

    default void validateNotDisabled() {
        validator().attribute(this::isDisabled, "disabled").isFalse();
    }
}
