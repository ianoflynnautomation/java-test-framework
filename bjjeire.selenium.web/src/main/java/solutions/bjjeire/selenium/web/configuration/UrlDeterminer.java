package solutions.bjjeire.selenium.web.configuration;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;


@Component
public class UrlDeterminer {

    private final UrlSettings urlSettings;

    /**
     * Constructor for Spring dependency injection.
     * @param urlSettings The URL settings loaded from the configuration file.
     */
    @Autowired
    public UrlDeterminer(UrlSettings urlSettings) {
        this.urlSettings = urlSettings;
    }

    /**
     * Constructs a full URL for an event page.
     * @param urlPart The specific path or part to append to the base event URL.
     * @return The complete, normalized URL as a string.
     */
    public String getEventUrl(String urlPart) {
        return contactUrls(urlSettings.getEventUrl(), urlPart);
    }

    /**
     * Constructs a full URL for a gym page.
     * @param urlPart The specific path or part to append to the base gym URL.
     * @return The complete, normalized URL as a string.
     */
    public String getGymUrl(String urlPart) {
        return contactUrls(urlSettings.getGymUrl(), urlPart);
    }

    private String contactUrls(String baseUrl, String pathPart) {
        try {
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.setPath(uriBuilder.getPath() + pathPart)
                    .build()
                    .normalize();
            return uri.toString();
        } catch (URISyntaxException ex) {
            // In a real application, you might want to log this error.
            // Returning null might hide configuration issues.
            throw new IllegalArgumentException("Failed to construct URL from base: " + baseUrl + " and part: " + pathPart, ex);
        }
    }
}