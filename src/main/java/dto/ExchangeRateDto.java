package dto;

import java.math.BigDecimal;

public interface ExchangeRateDto {
    Integer getId();

    CurrencyDto getBaseCurrency();

    CurrencyDto getTargetCurrency();

    BigDecimal getRate();
}
