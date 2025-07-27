package solutions.bjjeire.core.data.gyms;

import solutions.bjjeire.core.data.events.BjjEventType;

import java.util.Arrays;

public enum GymStatus {

    NONE("None"),
    ACTIVE("Active"),
    PENDING_APPROVAL("PendingApproval"),
    TEMPORARILY_CLOSED("TemporarilyClosed"),
    PERMANENTLY_CLOSED("PermanentlyClosed"),
    OPENING_SOON("OpeningSoon"),
    DRAFT("Draft"),
    REJECTED("Rejected");

    private final String gymStatus;

    GymStatus(String gymStatus)
    {
        this.gymStatus = gymStatus;
    }

    @Override
    public String toString() {
        return this.gymStatus;
    }

    public static GymStatus fromString(String text) {
        return Arrays.stream(values())
                .filter(type -> type.gymStatus.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No GymStatus constant with text '" + text + "' found"));
    }
}
