package solutions.bjjeire.core.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * A robust, thread-safe implementation of IConfigurationProvider.
 * It loads configuration from JSON files based on the current environment
 * (e.g., 'development', 'production'). The environment is determined by
 * system properties or a fallback `application.properties` file.
 * Loaded configurations are cached to avoid redundant file I/O.
 */
public class EnvironmentConfigurationProvider implements IConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfigurationProvider.class);
    private final String environment;
    private final JsonObject configRoot;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public EnvironmentConfigurationProvider() {
        this.environment = resolveEnvironment();
        logger.info("Initializing configuration for environment: '{}'", this.environment);
        String configFileName = String.format("testSettings.%s.json", this.environment);
        String jsonContent = loadResourceFile(configFileName);
        this.configRoot = JsonParser.parseString(jsonContent).getAsJsonObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSettings(Class<T> configClass) {
        // Use computeIfAbsent for thread-safe, efficient caching.
        return (T) cache.computeIfAbsent(configClass, clazz -> {
            String sectionName = getSectionName(clazz);
            logger.debug("Loading configuration for section: '{}'", sectionName);
            if (!configRoot.has(sectionName)) {
                throw new RuntimeException(String.format(
                        "Configuration section '%s' not found in 'testSettings.%s.json'.",
                        sectionName, environment));
            }
            String sectionJson = configRoot.get(sectionName).toString();
            return gson.fromJson(sectionJson, clazz);
        });
    }

    private String resolveEnvironment() {
        String envOverride = System.getProperty("environment");
        if (envOverride != null && !envOverride.isBlank()) {
            logger.info("Using environment from system property: '{}'", envOverride);
            return envOverride;
        }

        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null) {
                logger.warn("/application.properties not found. Defaulting to 'development' environment.");
                return "development";
            }
            Properties props = new Properties();
            props.load(input);
            String env = props.getProperty("environment", "development");
            logger.info("Using environment from application.properties: '{}'", env);
            return env;
        } catch (IOException e) {
            logger.error("Failed to load application.properties, defaulting to 'development'.", e);
            return "development";
        }
    }

    private String loadResourceFile(String fileName) {
        try (InputStream input = getClass().getResourceAsStream("/" + fileName)) {
            if (input == null) {
                throw new RuntimeException("Resource file not found: " + fileName);
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
