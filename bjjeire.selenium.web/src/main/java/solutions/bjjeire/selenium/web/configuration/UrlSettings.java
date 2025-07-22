package solutions.bjjeire.selenium.web.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "url-settings")
public class UrlSettings {
    private String eventUrl;
    private String gymUrl;
}
