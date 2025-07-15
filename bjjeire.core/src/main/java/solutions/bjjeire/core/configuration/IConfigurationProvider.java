package solutions.bjjeire.core.configuration;

/**
 * Defines the contract for a service that provides strongly-typed configuration objects.
 * This interface-based design allows for easy mocking and swapping of implementations.
 */
public interface IConfigurationProvider {
    /**
     * Retrieves a configuration section and maps it to a specified class.
     *
     * @param configClass The class representing the configuration section.
     * @param <T> The type of the configuration class.
     * @return An instance of the configuration class populated with settings.
     */
    <T> T getSettings(Class<T> configClass);
}