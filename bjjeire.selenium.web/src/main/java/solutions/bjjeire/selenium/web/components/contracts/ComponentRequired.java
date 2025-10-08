package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentRequired extends BjjEireComponent {

  boolean isRequired();

  default void validateIsSelected() {
    validator().attribute(this::isRequired, "required").isTrue();
  }

  default void validateNotSelected() {
    validator().attribute(this::isRequired, "required").isFalse();
  }
}
