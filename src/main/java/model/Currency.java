package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Currency {
    private final Integer id;
    private final String name;
    private final String code;
    private final String sign;
}
