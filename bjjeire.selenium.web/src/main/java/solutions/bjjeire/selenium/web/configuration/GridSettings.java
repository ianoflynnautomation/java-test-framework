package solutions.bjjeire.selenium.web.configuration;

import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GridSettings {
    private String providerName;
    private String optionsName;
    private String url;
    private List<HashMap<String, Object>> arguments;
}