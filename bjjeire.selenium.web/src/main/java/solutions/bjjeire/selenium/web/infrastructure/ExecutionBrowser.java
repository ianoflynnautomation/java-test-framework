package solutions.bjjeire.selenium.web.infrastructure;

import org.openqa.selenium.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionBrowser {
    Browser browser() default Browser.NOT_SET;
    Lifecycle lifecycle();
    int browserVersion() default 0;
    Platform platform() default Platform.ANY;
    int width() default 0;
    int height() default 0;
    DeviceName deviceName() default DeviceName.NOT_SET;
}