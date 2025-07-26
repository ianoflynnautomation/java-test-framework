package solutions.bjjeire.core.plugins;

import java.util.Arrays;

public enum Lifecycle {
    NOT_SET("not_set"),
    REUSE_IF_STARTED("reuse_if_started"),
    RESTART_EVERY_TIME("restart_every_time"),
    RESTART_ON_FAIL("restart_on_fail");

    private final String text;

    Lifecycle(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static Lifecycle fromText(String text) {
        return Arrays.stream(values())
                .filter(l -> l.text.equalsIgnoreCase(text) ||
                        l.text.replace("_", " ").equalsIgnoreCase(text) ||
                        l.text.replace("_", "").equalsIgnoreCase(text))
                .findFirst().orElse(Lifecycle.RESTART_EVERY_TIME);
    }
}