package dto;

import lombok.Getter;

@Getter
public class ExceptionDto implements Dto {
    String message;

    public ExceptionDto(String message) {
        this.message = message;
    }

}
