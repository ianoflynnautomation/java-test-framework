package solutions.bjjeire.selenium.web.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Determines and constructs URL paths for API endpoints.
 * This class uses Spring's UriComponentsBuilder for safe and reliable path construction,
 * making it compatible with WebClient.
 */
@Component
public class UrlDeterminer {

    private static final Logger log = LoggerFactory.getLogger(UrlDeterminer.class);
    private final UrlSettings urlSettings;

    @Autowired
    public UrlDeterminer(UrlSettings urlSettings) {
        this.urlSettings = urlSettings;
    }

    public String getEventUrl(String pathSuffix) {
        return buildUrl(urlSettings.getEventUrl(), pathSuffix);
    }

    public String getGymUrl(String pathSuffix) {
        return buildUrl(urlSettings.getGymUrl(), pathSuffix);
    }

    private String buildUrl(String basePath, String pathSuffix) {
        // UriComponentsBuilder is part of Spring and handles URL/URI construction robustly.
        String fullPath = UriComponentsBuilder.fromPath(basePath)
                .path(pathSuffix)
                .build()
                .toUriString();

        log.debug("Constructed URL path: {}", fullPath);
        return fullPath;
    }
}