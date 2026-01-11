package dto;

import java.math.BigDecimal;

public record ExchangeResponseDto (CurrenciesResponseDto baseCurrency, CurrenciesResponseDto targetCurrency,
                                   BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {
}
