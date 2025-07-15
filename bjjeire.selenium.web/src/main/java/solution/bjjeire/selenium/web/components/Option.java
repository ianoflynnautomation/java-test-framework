package solution.bjjeire.selenium.web.components;

public class Option extends WebComponent {
    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

//    @Override
    public boolean isDisabled() {
        return defaultGetDisabledAttribute();
    }

//    @Override
    public boolean isSelected() {
        return findElement().isSelected();
    }

//    @Override
    public String getText() {
        return defaultGetText();
    }

//    @Override
    public String getValue() {
        return defaultGetValue();
    }
}