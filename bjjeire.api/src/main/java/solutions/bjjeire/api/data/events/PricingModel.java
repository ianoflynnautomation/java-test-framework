package solutions.bjjeire.api.data.events;

import java.math.BigDecimal;

public record PricingModel(PricingType type, BigDecimal amount, int durationDays, String currency) {
}
