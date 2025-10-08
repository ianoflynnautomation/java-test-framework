package solutions.bjjeire.selenium.web.findstrategies;

import org.openqa.selenium.By;

public class TextFindStrategy extends FindStrategy {
  private final String text;

  public TextFindStrategy(String text) {
    super(buildXPath(text));
    this.text = text;
  }

  private static String buildXPath(String text) {
    return String.format(".//*[normalize-space(.)='%s']", text);
  }

  @Override
  public By convert() {
    return By.xpath(getValue());
  }

  @Override
  public String toString() {
    return String.format("by text = '%s'", text);
  }
}
