package solutions.bjjeire.core.utilities;

import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SingletonFactory extends ObjectFactory {
    private static final ThreadLocal<Map<Class<?>, Object>> mapHolder = ThreadLocal.withInitial(HashMap::new);

    public static <T> T getInstance(Class<T> classOf, Object... initargs) {
        if (!mapHolder.get().containsKey(classOf)) {
            T obj = tryGetInstance(classOf, initargs);
            register(obj);
        }

        return (T)mapHolder.get().get(classOf);
    }

    private static <T> T tryGetInstance(Class<T> classOf, Object... initargs) {
        try {
            return newInstance(classOf, initargs);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 ConstructorNotFoundException e) {
            //Log.error("Failed to create instance of the class %s.\nException was:\n%s".formatted(classOf.getName(), e));
            return null;
        }
    }

    public static <T> void register(T instance) {
        if (instance != null)
            mapHolder.get().put(instance.getClass(), instance);
    }

    public static <T> void register(Class<?> classKey, T instance) {
        if (instance != null)
            mapHolder.get().put(classKey, instance);
    }

    public static boolean containsKey(Class<?> classOf) {
        return mapHolder.get().containsKey(classOf);
    }

    public static boolean containsValue(Object object) {
        return mapHolder.get().containsValue(object);
    }

    public static void clear() {
        mapHolder.get().clear();
    }
}
