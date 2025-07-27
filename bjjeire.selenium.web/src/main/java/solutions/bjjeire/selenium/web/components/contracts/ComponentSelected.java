package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentSelected extends BjjEireComponent {

    boolean isSelected();

    default void validateIsSelected() {
        validator().attribute(this::isSelected, "selected").isTrue();
    }

    default void validateNotSelected() {
        validator().attribute(this::isSelected, "selected").isFalse();
    }

}
