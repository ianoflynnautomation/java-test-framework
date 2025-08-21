package solutions.bjjeire.cucumber.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.cucumber.datatable.DataTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.context.TestDataContext;
import solutions.bjjeire.cucumber.data.GymDataRow;
import solutions.bjjeire.selenium.web.data.TestDataManager;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GymActions {

    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final TestDataContext testDataContext;

    public void createGyms(DataTable dataTable) {

        List<GymDataRow> dataRows = dataTable.asList(GymDataRow.class);

        List<Gym> gymsToCreate = dataRows.stream()
                .map(row -> GymFactory.createGym(builder -> builder
                        .name(row.getName())
                        .county(row.getCounty())))
                .collect(Collectors.toList());

        String authToken = scenarioContext.getAuthToken();
        List<String> createdIds = testDataManager.seed(gymsToCreate, authToken);

        testDataContext.addEntityIds(Gym.class, createdIds);
        log.info("Seeded {} BJJ gym(s) for the test.", createdIds.size());
    }
}
