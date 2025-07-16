package solutions.bjjeire.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

///**
// * @deprecated This static utility class is a legacy component.
// * Use the Spring-managed {@link EnvironmentConfigurationProvider} bean instead,
// * which is more robust, testable, and aligns with modern dependency injection principles.
// */
@UtilityClass
public final class ConfigurationService {
    private static String environment;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getEnvironment() {
        return environment;
    }

    @SneakyThrows
    public static <T> T get(Class<T> configSection) {
        if (environment == null) {
            String environmentOverride = System.getProperty("environment");
            if (environmentOverride == null) {
                try (InputStream input = ConfigurationService.class.getResourceAsStream("/application.properties")) {
                    var p = new Properties();
                    p.load(input);
                    environment = p.getProperty("environment");
                } catch (IOException | NullPointerException e) {
                    throw new RuntimeException("Could not load environment from application.properties", e);
                }
            } else {
                environment = environmentOverride;
            }
        }

        String fileName = String.format("testSettings.%s.json", environment);
        String jsonFileContent = getFileAsString(fileName);
        String sectionName = getSectionName(configSection);

        var sectionNode = objectMapper.readTree(jsonFileContent).get(sectionName);
        if (sectionNode == null) {
            throw new RuntimeException("Configuration section '" + sectionName + "' not found in " + fileName);
        }

        return objectMapper.treeToValue(sectionNode, configSection);
    }

    public static String getSectionName(Class<?> configSection) {
        var sb = new StringBuilder(configSection.getSimpleName());
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    @SneakyThrows
    public static String getFileAsString(String fileName) {
        try (InputStream input = ConfigurationService.class.getResourceAsStream("/" + fileName)) {
            return IOUtils.toString(Objects.requireNonNull(input), StandardCharsets.UTF_8);
        }
    }
}
