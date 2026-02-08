package service;

import dto.ExchangeDto;

import java.math.BigDecimal;
import java.util.Optional;

public interface Service {
    Optional<ExchangeDto> convert(String baseCode, String targetCode, BigDecimal amount);
}
