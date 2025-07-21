package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentChecked extends BjjEireComponent {

    boolean isChecked();

    default void validateIsChecked() {
        validator().attribute(this::isChecked, "checked").isTrue();
    }

    default void validateIsUnchecked() {
        validator().attribute(this::isChecked, "checked").isFalse();
    }


    default void validateIsChecked(boolean expectedState) {
        if (expectedState) {
            validateIsChecked();
        } else {
            validateIsUnchecked();
        }
    }
}
