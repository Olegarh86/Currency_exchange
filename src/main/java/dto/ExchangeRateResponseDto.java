package dto;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(int id, CurrenciesResponseDto baseCurrency, CurrenciesResponseDto targetCurrency,
                                      BigDecimal rate) {
}
