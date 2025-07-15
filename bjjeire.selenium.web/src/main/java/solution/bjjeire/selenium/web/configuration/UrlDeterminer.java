package solution.bjjeire.selenium.web.configuration;

import org.apache.http.client.utils.URIBuilder;
import solutions.bjjeire.core.configuration.ConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlDeterminer {

    public static String getEventUrl(String urlPart) {
        return contactUrls(ConfigurationService.get(UrlSettings.class).getEventUrl(), urlPart);
    }

    public static String getGymUrl(String urlPart) {
        return contactUrls(ConfigurationService.get(UrlSettings.class).getGymUrl(), urlPart);
    }

    private static String contactUrls(String url, String part) {
        try {
            var uriBuilder = new URIBuilder(url);
            URI uri = uriBuilder.setPath(uriBuilder.getPath() + part)
                    .build()
                    .normalize();
            return uri.toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
