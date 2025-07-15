package solution.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class RoleFindStrategy extends FindStrategy {
    private final String role;
    private final String accessibleName;

    public RoleFindStrategy(String role, String... name) {
        super(buildXPath(role, name));
        this.role = role;
        this.accessibleName = (name.length > 0 && name[0] != null && !name[0].isEmpty()) ? name[0] : null;
    }

    private static String buildXPath(String role, String... name) {
        String accessibleName = (name.length > 0 && name[0] != null && !name[0].isEmpty()) ? name[0] : null;

        if (accessibleName != null) {
            return String.format(".//*[@role='%s' and (normalize-space(.)='%s' or @aria-label='%s')]", role, accessibleName, accessibleName);
        } else {
            return String.format(".//*[@role='%s']", role);
        }
    }

    @Override
    public By convert() {
        return By.xpath(getValue());
    }

    @Override
    public String toString() {
        if (accessibleName != null) {
            return String.format("role = '%s' with name = '%s'", role, accessibleName);
        } else {
            return String.format("role = '%s'", role);
        }
    }
}