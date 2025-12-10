package dto;

import java.math.BigDecimal;

public record CurrenciesFilter(String base_currency_code,
                               String target_currency_code) {
}
