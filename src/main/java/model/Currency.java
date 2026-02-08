package model;

public record Currency(Long id, String name, String code, String sign) {
    public Currency(String name, String code, String sign) {
        this(0L, name, code, sign);
    }
}
