package Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DailySchedule(DayOfWeek day, LocalTime openTime, LocalTime closeTime) {
}
