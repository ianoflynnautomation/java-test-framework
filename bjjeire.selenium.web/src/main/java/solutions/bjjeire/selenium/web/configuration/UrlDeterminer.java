package solutions.bjjeire.selenium.web.configuration;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class UrlDeterminer {

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
    String fullPath =
        UriComponentsBuilder.fromPath(basePath).path(pathSuffix).build().toUriString();

    log.debug(
        "Constructed URL",
        StructuredArguments.keyValue("basePath", basePath),
        StructuredArguments.keyValue("pathSuffix", pathSuffix),
        StructuredArguments.keyValue("fullPath", fullPath));
    return fullPath;
  }
}
