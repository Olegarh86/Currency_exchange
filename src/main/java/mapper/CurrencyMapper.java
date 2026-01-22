package mapper;

import dto.CurrencyDto;
import dto.CurrencyResponseDto;
import dto.ExchangeRateDto;
import dto.ExchangeRateResponseDto;
import model.Currency;
import model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

//    @Mapping(target = "id", ignore = true)
    Currency convertCurrencyDtoToCurrency(CurrencyDto currencyDto);

    CurrencyResponseDto convertCurrencyToDto(Currency currency);

    ExchangeRateResponseDto convertExchangeRateToExchangeRateDto(ExchangeRate exchangeRate);

    ExchangeRate convertExchangeRateDtoToExchangeRate(ExchangeRateDto exchangeRateDto);
}
