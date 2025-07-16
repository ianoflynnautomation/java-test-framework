package Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DailySchedule(String day, LocalTime openTime, LocalTime closeTime) {
}
