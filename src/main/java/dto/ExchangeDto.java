package dto;

import model.Currency;

import java.math.BigDecimal;

public record ExchangeDto(Currency baseCurrency, Currency targetCurrency,
                          BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {
}
