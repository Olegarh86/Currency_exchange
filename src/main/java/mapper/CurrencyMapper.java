package mapper;

import dto.*;
import model.Currency;
import model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    CurrencyResponseDto currencyToDto(Currency currency);

    CurrencyResponseDto requestDtoToResponseDto(Long id, CurrencyRequestDto requestDto);

    Currency dtoToCurrency(CurrencyRequestDto currencyDto);

    @Mapping(target = "id",  ignore = true)
    ExchangeRate currenciesWithRateToExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate);

    ExchangeRateResponseDto exchangeRateToDto(ExchangeRate exchangeRate);

    ExceptionDto exceptionToDto(String message);

    ExchangeDto exchangeResultToDto(Currency baseCurrency, Currency targetCurrency,
                                    BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount);
}
