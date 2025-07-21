package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentValue extends BjjEireComponent{

    String getValue();

    default void validateValueIs(String value) {
        validator().attribute(this::getValue, "value").is(value);
    }

    default void validateValueContains(String value) {
        validator().attribute(this::getValue, "value").contains(value);
    }

    default void validateValueNotContains(String value) {
        validator().attribute(this::getValue, "value").notContains(value);
    }
}
