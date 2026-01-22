package dto;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(int id, CurrencyDto baseCurrency, CurrencyDto targetCurrency,
                                      BigDecimal rate) implements ExchangeRateDto {
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public CurrencyDto getBaseCurrency() {
        return baseCurrency;
    }

    @Override
    public CurrencyDto getTargetCurrency() {
        return targetCurrency;
    }

    @Override
    public BigDecimal getRate() {
        return rate;
    }
}
