package dao;

import model.Currency;

import java.util.Optional;

public interface CurrencyDao extends CrudDao<Currency>{

    Optional<Currency> findByCode(String code);
}
