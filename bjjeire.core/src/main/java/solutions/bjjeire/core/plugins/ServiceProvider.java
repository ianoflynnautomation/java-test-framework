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

  public static <T> T getService(Class<T> classToGet) {
    return applicationContext.getBean(classToGet);
  }
}
