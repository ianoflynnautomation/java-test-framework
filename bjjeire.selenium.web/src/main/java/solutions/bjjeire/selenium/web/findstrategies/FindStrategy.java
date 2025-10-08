package solutions.bjjeire.selenium.web.findstrategies;

import lombok.Getter;
import org.openqa.selenium.By;

@Getter
public abstract class FindStrategy {

  private final String value;

  protected FindStrategy(String value) {
    this.value = value;
  }

  public abstract By convert();
}
