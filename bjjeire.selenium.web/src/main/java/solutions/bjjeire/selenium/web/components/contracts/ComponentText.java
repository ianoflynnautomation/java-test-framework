package solutions.bjjeire.selenium.web.components.contracts;

public interface ComponentText extends BjjEireComponent {
    String getText();

    default void validateTextIs(String value) {
        validator().attribute(this::getText, "innerText").is(value);
    }

    default void validateTextContains(String value) {
        validator().attribute(this::getText, "innerText").contains(value);
    }

    default void validateTextIsSet() {
        validator().attribute(() -> !getText().isEmpty(), "innerText").isTrue();
    }
}