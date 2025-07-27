package solutions.bjjeire.selenium.web.configuration;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class UrlDeterminer {

    private static final Logger log = LoggerFactory.getLogger(UrlDeterminer.class);
    private final UrlSettings urlSettings;

    @Autowired
    public UrlDeterminer(UrlSettings urlSettings) {
        this.urlSettings = urlSettings;
    }

    public String getEventUrl(String urlPart) {
        return contactUrls(urlSettings.getEventUrl(), urlPart);
    }

    public String getGymUrl(String urlPart) {
        return contactUrls(urlSettings.getGymUrl(), urlPart);
    }

    private String contactUrls(String baseUrl, String pathPart) {
        try {
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.setPath(uriBuilder.getPath() + pathPart)
                    .build()
                    .normalize();
            String fullUrl = uri.toString();
            log.debug("Constructed URL: {}", fullUrl);
            return fullUrl;
        } catch (URISyntaxException ex) {
            log.error("Failed to construct URL from base: '{}' and part: '{}'", baseUrl, pathPart, ex);
            throw new IllegalArgumentException("Failed to construct URL.", ex);
        }
    }
}