package mapper;

import dto.CurrencyDto;
import dto.CurrencyResponseDto;
import dto.ExchangeRateDto;
import dto.ExchangeRateResponseDto;
import model.Currency;
import model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    Currency dtoToCurrency(CurrencyDto currencyDto);

    CurrencyResponseDto currencyToDto(Currency currency);

    ExchangeRateResponseDto exchangeRateToDto(ExchangeRate exchangeRate);

    ExchangeRate dtoToExchangeRate(ExchangeRateDto exchangeRateDto);
}
