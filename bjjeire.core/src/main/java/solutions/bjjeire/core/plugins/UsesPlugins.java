package solutions.bjjeire.core.plugins;

import java.util.ArrayList;
import java.util.List;

public abstract class UsesPlugins {

    private static final ThreadLocal<List<Plugin>> PLUGINS = ThreadLocal.withInitial(ArrayList::new);

    public static List<Plugin> getPlugins() {
        return PLUGINS.get();
    }

    protected void addPlugin(Class<? extends Plugin> pluginClass) {
        Plugin plugin = ServiceProvider.getService(pluginClass);
        if (plugin != null) {
            getPlugins().add(plugin);
        }
    }

    protected static void VETO_PLUGINS() {
        PLUGINS.get().clear();
    }
}