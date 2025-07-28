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

    /**
     * Constructs a URL path for the event endpoint.
     * @param pathSuffix The part of the path to append, e.g., an event ID.
     * @return A complete path string for the event resource.
     */
    public String getEventUrl(String pathSuffix) {
        return buildUrl(urlSettings.getEventUrl(), pathSuffix);
    }

    /**
     * Constructs a URL path for the gym endpoint.
     * @param pathSuffix The part of the path to append, e.g., a gym ID.
     * @return A complete path string for the gym resource.
     */
    public String getGymUrl(String pathSuffix) {
        return buildUrl(urlSettings.getGymUrl(), pathSuffix);
    }

    /**
     * Safely combines a base path from settings with a suffix path.
     * This method correctly handles leading or trailing slashes.
     *
     * @param basePath The base path from UrlSettings (e.g., "/api/event").
     * @param pathSuffix The additional path segment (e.g., "/123").
     * @return The combined path string (e.g., "/api/event/123").
     */
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