package solutions.bjjeire.cucumber.data;

import lombok.Data;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEventType;

@Data
public class EventDataRow {
  private String name;
  private County county;
  private BjjEventType type;
}
