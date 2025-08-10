package solutions.bjjeire.cucumber.configuration;

import io.cucumber.java.DataTableType;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.steps.events.EventFilteringSteps;
import solutions.bjjeire.cucumber.steps.gyms.GymFilteringSteps;

import java.util.Map;

public class DataTableTransformer {

    @DataTableType
    public EventFilteringSteps.EventDataRow eventDataRowTransformer(Map<String, String> entry) {
        EventFilteringSteps.EventDataRow row = new EventFilteringSteps.EventDataRow();
        row.setName(entry.get("Name"));

        if (entry.get("County") != null) {
            row.setCounty(County.valueOf(entry.get("County").replace(" ", "")));
        }
        if (entry.get("Type") != null) {
            row.setType(BjjEventType.fromString(entry.get("Type")));
        }
        return row;
    }

    @DataTableType
    public GymFilteringSteps.GymDataRow gymDataRowTransformer(Map<String, String> entry) {
        GymFilteringSteps.GymDataRow row = new GymFilteringSteps.GymDataRow();
        row.setName(entry.get("Name"));
        if (entry.get("County") != null) {
            row.setCounty(County.valueOf(entry.get("County").replace(" ", "")));
        }
        return row;
    }
}