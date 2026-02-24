package service;

import dto.ExchangeDto;

import java.math.BigDecimal;

public interface Service {
    ExchangeDto convert(String baseCode, String targetCode, BigDecimal amount);
}
