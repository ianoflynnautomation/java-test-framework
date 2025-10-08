package solutions.bjjeire.core.data.events;

import java.util.Arrays;

public enum BjjEventType {
  OPEN_MAT("Open Mat"),

  SEMINAR("Seminar"),

  TOURNAMENT("Tournament"),

  CAMP("Camp"),

  OTHER("Other");

  private final String eventType;

  BjjEventType(String eventType) {
    this.eventType = eventType;
  }

  @Override
  public String toString() {
    return this.eventType;
  }

  public static BjjEventType fromString(String text) {
    return Arrays.stream(values())
        .filter(type -> type.eventType.equalsIgnoreCase(text))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No BjjEventType constant with text '" + text + "' found"));
  }
}
