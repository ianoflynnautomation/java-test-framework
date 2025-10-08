package solutions.bjjeire.cucumber.data;

import lombok.Data;
import solutions.bjjeire.core.data.common.County;

@Data
public class GymDataRow {
  private String name;
  private County county;
}
