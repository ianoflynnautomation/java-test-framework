package solutions.bjjeire.core.plugins;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TimeRecord {
    private long startTime;
    private long endTime;

    public long getDuration() {
        return endTime - startTime;
    }
}