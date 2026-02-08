package dto;

import java.math.BigDecimal;

public record ExchangeRateRequestDto(String baseCode, String targetCode,
                                     BigDecimal rate) {
}
