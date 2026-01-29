package dto;

import java.math.BigDecimal;

public interface ExchangeRateDto extends Dto {
    Integer getId();

    CurrencyDto getBaseCurrency();

    CurrencyDto getTargetCurrency();

    BigDecimal getRate();
}
