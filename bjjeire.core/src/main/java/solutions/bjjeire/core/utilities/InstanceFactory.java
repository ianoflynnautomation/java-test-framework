package solutions.bjjeire.core.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

public final class InstanceFactory extends ObjectFactory {
    public static <T> T create(Class<T> classOf) {
        return tryGetInstance(classOf);
    }

    public static <T> T create(Class<T> classOf, Object... args) {
        return tryGetInstance(classOf, args);
    }

    public static <T> T createByTypeParameter(Class parameterClass, int index) {
        return tryCreateByTypeParameter(parameterClass, index);
    }

    private static <T> T tryCreateByTypeParameter(Class parameterClass, int index) {
        try {
            var elementsClass = (Class<T>)((ParameterizedType)parameterClass.getGenericSuperclass()).getActualTypeArguments()[index];
            return tryGetInstance(elementsClass);
        } catch (ClassCastException e) {
            //Log.error("Failed to create instance of the class %s.\nIt seems that the class was not parametrized! Exception was:\n%s".formatted(parameterClass.getName(), e));
        } catch (IndexOutOfBoundsException e) {
            //Log.error("Failed to create instance of the class %s.\nIt seems that the index provided was not a valid one. Exception was:\n%s".formatted(parameterClass.getName(), e));
        } catch (Exception e) {
            //Log.error("Failed to create instance of the class %s.\nException was:\n%s".formatted(parameterClass.getName(), e));
        }

        return null;
    }

    private static <T> T tryGetInstance(Class<T> classOf, Object... initargs) {
        try {
            return newInstance(classOf, initargs);
        } catch (InvocationTargetException|InstantiationException|IllegalAccessException|ConstructorNotFoundException e) {
            //Log.error("Failed to create instance of the class %s.\nException was:\n%s".formatted(classOf.getName(), e));
            return null;
        }
    }
}