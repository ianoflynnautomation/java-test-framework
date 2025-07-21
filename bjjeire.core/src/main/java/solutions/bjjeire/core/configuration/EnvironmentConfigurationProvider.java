package solutions.bjjeire.core.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A robust, thread-safe implementation of IConfigurationProvider using Jackson for JSON processing.
 * It loads configuration from a JSON file corresponding to the specified environment.
 */
@Component
public class EnvironmentConfigurationProvider implements IConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfigurationProvider.class);
    private final String environment;
    private final JsonNode configRoot;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * FIX: This class is now a component, and its dependencies (the environment name)
     * are injected by Spring. This resolves bean initialization order issues.
     *
     * @param environment The name of the environment (e.g., "development"),
     * injected from application.properties.
     */
    @Autowired
    public EnvironmentConfigurationProvider(@Value("${environment:development}") String environment) {
        this.environment = environment;
        logger.info("Initializing configuration for environment: '{}'", this.environment);
        String configFileName = String.format("testSettings.%s.json", this.environment);
        String jsonContent = loadResourceFile(configFileName);
        try {
            this.configRoot = objectMapper.readTree(jsonContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse configuration file: " + configFileName, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSettings(Class<T> configClass) {
        return (T) cache.computeIfAbsent(configClass, clazz -> {
            String sectionName = getSectionName(clazz);
            logger.debug("Loading configuration for section: '{}'", sectionName);

            JsonNode sectionNode = configRoot.get(sectionName);
            if (sectionNode == null || sectionNode.isNull()) {
                throw new RuntimeException(String.format(
                        "Configuration section '%s' not found or is null in 'testSettings.%s.json'.",
                        sectionName, environment));
            }

            try {
                return objectMapper.treeToValue(sectionNode, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to map configuration section '" + sectionName + "' to class " + clazz.getSimpleName(), e);
            }
        });
    }

    private String loadResourceFile(String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Resource file not found on classpath: " + fileName);
            }
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource file: " + fileName, e);
        }
    }

    private String getSectionName(Class<?> configClass) {
        String simpleName = configClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}