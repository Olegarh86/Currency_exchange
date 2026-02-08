package dto;

import model.Currency;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(Long id, Currency baseCurrency, Currency targetCurrency,
                                      BigDecimal rate) {
}
