package solutions.bjjeire.core.utilities;

import java.util.HashMap;
import java.util.function.Supplier;

public class SecretsResolver {
    private static final ThreadLocal<HashMap<String, String>> CACHED_SECRETS;

    static {
        CACHED_SECRETS = ThreadLocal.withInitial(HashMap::new);
        CACHED_SECRETS.set(new HashMap<>());
    }

    public static String getSecret(Supplier<String> getConfigValue) {
        return getSecret(getConfigValue.get());
    }

    public static String getSecret(String configValue) {
        if (CACHED_SECRETS.get().containsKey(configValue)) {
            return CACHED_SECRETS.get().get(configValue);
        }

        if (configValue.contains("env_")) {
            var envName = configValue.replace("{env_", "").replace("}", "").toLowerCase();
            String environmentalVariable = System.getenv(envName);
            CACHED_SECRETS.get().put(configValue, environmentalVariable);
            return environmentalVariable;
        } else {
            return configValue;
        }
    }
}