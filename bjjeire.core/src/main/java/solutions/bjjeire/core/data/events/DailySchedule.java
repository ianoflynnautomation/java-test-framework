package solutions.bjjeire.api.data.events;

import java.time.LocalTime;

public record DailySchedule(String day, LocalTime openTime, LocalTime closeTime) {
}
