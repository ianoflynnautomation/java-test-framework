package solutions.bjjeire.core.plugins;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ServiceProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Retrieves a Spring-managed bean of the specified type.
     *
     * @param classToGet The class of the bean to retrieve.
     * @param <T> The type of the bean.
     * @return The singleton instance of the bean from the Spring context.
     */
    public static <T> T getService(Class<T> classToGet) {
        return applicationContext.getBean(classToGet);
    }
}