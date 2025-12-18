package dto;

public record FindExchangeRateByIdDto(String base_currency_code,
                                      String target_currency_code) {
}
