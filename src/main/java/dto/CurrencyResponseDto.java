package dto;


public record CurrencyResponseDto(Integer id, String name, String code, String sign) implements CurrencyDto {
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCode() {
        return code;
    }
}
