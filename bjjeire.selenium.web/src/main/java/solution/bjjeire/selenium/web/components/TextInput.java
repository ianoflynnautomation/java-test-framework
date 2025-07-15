package solution.bjjeire.selenium.web.components;

public class TextInput extends WebComponent {

    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

//    @Override
    public String getText() {
        String text = defaultGetText();

        if (text.isEmpty()) {
            return defaultGetValue();
        }

        return text;
    }

    public void setText(String value) {
        defaultSetText(value);
    }

//    @Override
    public String getValue() {
        return defaultGetValue();
    }


}
