package Data;

import java.math.BigDecimal;

public record PricingModel(PricingType type, BigDecimal amount, int durationDays, String currency) {
}
