package dto;


public record CurrencyRequestDto(String name, String code, String sign) implements CurrencyDto {

    public CurrencyRequestDto(String code) {
        this("", code, "");
    }

    @Override
    public Integer getId() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getSign() {
        return sign;
    }
}
