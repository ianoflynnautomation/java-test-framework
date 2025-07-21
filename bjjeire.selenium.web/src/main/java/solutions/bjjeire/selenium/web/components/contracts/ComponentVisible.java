package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentVisible extends BjjEireComponent{

    boolean isVisible();


    default void validateIsVisible() {
        validator().attribute(this::isVisible, "visible").isTrue();
    }

    default void validateIsNotVisible() {
        validator().attribute(this::isVisible, "visible").isFalse();
    }
}
