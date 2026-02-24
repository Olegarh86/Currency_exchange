package dto;

import model.Currency;

import java.math.BigDecimal;

public record CurrenciesWithRateDto(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
}
