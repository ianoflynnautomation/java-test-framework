package solution.bjjeire.selenium.web.components;

public class Span extends WebComponent{
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
