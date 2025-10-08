package solutions.bjjeire.cucumber.actions;

import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.context.TestDataContext;
import solutions.bjjeire.cucumber.data.EventDataRow;
import solutions.bjjeire.selenium.web.data.TestDataManager;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EventActions {

  private final TestDataManager testDataManager;
  private final ScenarioContext scenarioContext;
  private final TestDataContext testDataContext;

  public void createEvents(DataTable dataTable) {

    List<EventDataRow> dataRows = dataTable.asList(EventDataRow.class);

    List<BjjEvent> eventsToCreate =
        dataRows.stream()
            .map(
                row ->
                    BjjEventFactory.createBjjEvent(
                        builder ->
                            builder
                                .name(row.getName())
                                .county(row.getCounty())
                                .type(row.getType())))
            .collect(Collectors.toList());

    String authToken = scenarioContext.getAuthToken();
    List<String> createdIds = testDataManager.seed(eventsToCreate, authToken);

    testDataContext.addEntityIds(BjjEvent.class, createdIds);
    log.info("Seeded {} BJJ event(s) via API.", createdIds.size());
  }
}
