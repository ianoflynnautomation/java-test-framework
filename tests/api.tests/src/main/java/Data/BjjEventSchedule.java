package Data;

import java.time.LocalDate;
import java.util.List;

public record BjjEventSchedule(ScheduleType scheduleType, LocalDate startDate, LocalDate endDate, List<DailySchedule> hours) {
}
