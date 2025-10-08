package solutions.bjjeire.cucumber.transformer;

import io.cucumber.java.DataTableType;
import java.util.Map;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.cucumber.data.GymDataRow;

public class GymDataTableTransformer {

  @DataTableType
  public GymDataRow gymDataRowTransformer(Map<String, String> entry) {
    GymDataRow row = new GymDataRow();
    row.setName(entry.get("Name"));
    if (entry.get("County") != null) {
      row.setCounty(County.valueOf(entry.get("County").replace(" ", "")));
    }
    return row;
  }
}
