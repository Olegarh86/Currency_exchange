package dto;


public record CurrencyRequestDto(String name, String code, String sign) implements CurrencyDto {

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

    public CurrencyRequestDto(String code) {
        this("", code, "");
    }
}
