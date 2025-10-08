package solutions.bjjeire.core.data.events;

import java.math.BigDecimal;

public record PricingModel(
    PricingType type, BigDecimal amount, int durationDays, String currency) {}
