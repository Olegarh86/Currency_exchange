package mapper;

import dto.CurrenciesResponseDto;
import model.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    CurrenciesResponseDto convertCurrencyToDto(Currency currency);
}
