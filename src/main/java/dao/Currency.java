package dao;

import dto.CurrenciesResponseDto;

import java.util.List;

public interface Currency {

    List<CurrenciesResponseDto> getAllCurrencies();
}
