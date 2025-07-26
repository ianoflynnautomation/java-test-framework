package solutions.bjjeire.api.data.events;

import java.time.LocalDate;
import java.util.List;

public record BjjEventSchedule(ScheduleType scheduleType, LocalDate startDate, LocalDate endDate, List<DailySchedule> hours) {
}
