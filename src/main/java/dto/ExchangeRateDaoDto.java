package dto;

import java.math.BigDecimal;

public record ExchangeRateDaoDto(int id, int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
}
