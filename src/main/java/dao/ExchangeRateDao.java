package dao;

import model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateDao extends CrudDao<ExchangeRate>{

    void updateRate(ExchangeRate exchangeRate);
    Optional<ExchangeRate> findByCodes(String baseCode, String targetCode);
}
