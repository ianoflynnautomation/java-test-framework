package solutions.bjjeire.cucumber.transformer;

import io.cucumber.java.DataTableType;
import java.util.Map;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.data.EventDataRow;

public class EventDataTableTransformer {

  @DataTableType
  public EventDataRow eventDataRowTransformer(Map<String, String> entry) {
    EventDataRow row = new EventDataRow();
    row.setName(entry.get("Name"));

    if (entry.get("County") != null) {
      row.setCounty(County.valueOf(entry.get("County").replace(" ", "")));
    }
    if (entry.get("Type") != null) {
      row.setType(BjjEventType.fromString(entry.get("Type")));
    }
    return row;
  }
}
