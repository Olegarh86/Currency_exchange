package dto;

import java.math.BigDecimal;

public record ExchangeRequestDto(String baseCode, String targetCode, BigDecimal amount) {
}
