package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentTarget extends BjjEireComponent {
  String getTarget();

  default void validateTargetIs(String value) {
    validator().attribute(this::getTarget, "target").is(value);
  }

  default void validateTargetIsSet() {
    validator().attribute(() -> !getTarget().isEmpty(), "target").isTrue();
  }

  default void validateTargetNotSet() {
    validator().attribute(() -> getTarget().isEmpty(), "target").isTrue();
  }
}
