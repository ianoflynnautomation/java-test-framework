package solution.bjjeire.selenium.web.components;

public class Div extends WebComponent {

    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

    public String getHtml() {
        return defaultGetInnerHtmlAttribute();
    }

    public String getText() {
        return defaultGetText();
    }
}
