package solutions.bjjeire.cucumber.configuration;

import io.cucumber.java.DataTableType;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.steps.events.EventFilteringSteps;
import solutions.bjjeire.cucumber.steps.gyms.GymFilteringSteps;

import java.util.Map;

/**
 * This class registers custom DataTable transformers for Cucumber.
 * It tells Cucumber how to convert a table row from a feature file
 * into a specific Java object (DTO).
 */
public class DataTableTransformer {

    /**
     * Transforms a map of table data into an EventDataRow object.
     * Cucumber will automatically use this when it sees a DataTable
     * that needs to be converted to a List<EventDataRow>.
     *
     * @param entry A map representing one row of the DataTable.
     * @return A populated EventDataRow object.
     */
    @DataTableType
    public EventFilteringSteps.EventDataRow eventDataRowTransformer(Map<String, String> entry) {
        EventFilteringSteps.EventDataRow row = new EventFilteringSteps.EventDataRow();
        row.setName(entry.get("Name"));
        // Handle potential spaces in County names from the feature file
        if (entry.get("County") != null) {
            row.setCounty(County.valueOf(entry.get("County").replace(" ", "")));
        }
        if (entry.get("Type") != null) {
            row.setType(BjjEventType.fromString(entry.get("Type")));
        }
        return row;
    }

    /**
     * Transforms a map of table data into a GymDataRow object.
     */
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