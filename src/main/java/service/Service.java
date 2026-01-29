package service;

import dto.CurrencyDto;
import dto.Dto;

import java.math.BigDecimal;

public interface Service {
    Dto convert(CurrencyDto currencyDtoBase, CurrencyDto currencyDtoTarget, BigDecimal amount);
}
