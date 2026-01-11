package dto;

import java.math.BigDecimal;

public record ExchangeRateRequestDto (CurrenciesResponseDto baseCurrency, CurrenciesResponseDto targetCurrency,
                                      BigDecimal rate) {
}
