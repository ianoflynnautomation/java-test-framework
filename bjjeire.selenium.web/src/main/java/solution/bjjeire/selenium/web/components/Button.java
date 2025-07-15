package solution.bjjeire.selenium.web.components;

public class Button extends WebComponent {
    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

    public void click() {
        defaultClick();
    }
}
