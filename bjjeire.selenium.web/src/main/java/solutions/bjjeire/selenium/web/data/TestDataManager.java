package solutions.bjjeire.selenium.web.data;

import java.util.List;

public interface TestDataManager {

    String authenticate();

    <T> List<String> seed(List<T> entities, String authToken);

    <T> void teardown(Class<T> entityType, List<String> ids, String authToken);

}