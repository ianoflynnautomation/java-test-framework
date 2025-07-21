package solutions.bjjeire.selenium.web.components.enums;

import lombok.Getter;

@Getter
public enum AriaRole {
    HEADING("heading"),
    BUTTON("button"),
    LINK("link"),
    CHECKBOX("checkbox"),
    RADIO("radio"),
    TEXTBOX("textbox"),
    ALERT("alert"),
    DIALOG("dialog");

    private final String roleName;

    AriaRole(String roleName) {
        this.roleName = roleName;
    }

}