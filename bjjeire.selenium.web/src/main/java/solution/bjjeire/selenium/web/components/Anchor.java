package solution.bjjeire.selenium.web.components;

public class Anchor extends WebComponent {

    public void click() {
        defaultClick();
    }

    public String getHref() {
        return defaultGetHref();
    }

    public String getText() {
        return defaultGetText();
    }

    public String getHtml() {
        return defaultGetInnerHtmlAttribute();
    }

    public String getTarget() {
        return defaultGetTargetAttribute();
    }

    public String getRel() {
        return defaultGetRelAttribute();
    }
}
